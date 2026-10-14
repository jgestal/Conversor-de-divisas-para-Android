package es.gestal.conversordivisas.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import es.gestal.conversordivisas.data.DivisaInfo
import es.gestal.conversordivisas.data.DivisasCatalogo
import es.gestal.conversordivisas.data.PreferenciasDivisas
import es.gestal.conversordivisas.data.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

/**
 * ViewModel que gestiona el estado reactivo, la persistencia local,
 * la reordenación de favoritas y las llamadas a la API.
 */
class ConversorViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferenciasDivisas(application)

    private val favoritosIniciales = prefs.obtenerFavoritos()
    private val divisaActivaInicial = prefs.obtenerDivisaActiva().let { activa ->
        if (favoritosIniciales.contains(activa)) activa
        else favoritosIniciales.firstOrNull() ?: ""
    }

    private val _uiState = MutableStateFlow(
        ConversorUiState(
            divisaActiva = divisaActivaInicial,
            importeBase = prefs.obtenerUltimoImporte(),
            favoritos = favoritosIniciales,
            tasas = prefs.obtenerTasas(),
            ultimaActualizacion = prefs.obtenerUltimaActualizacion()
        )
    )
    val uiState: StateFlow<ConversorUiState> = _uiState.asStateFlow()

    init {
        if (_uiState.value.divisaActiva.isNotBlank()) {
            cargarTasas(divisaBase = _uiState.value.divisaActiva)
        }
    }

    /**
     * Descarga las tasas de cambio de la API de Frankfurter con soporte offline-first.
     */
    fun cargarTasas(divisaBase: String) {
        if (divisaBase.isBlank()) {
            _uiState.update { it.copy(cargando = false, tasas = emptyMap()) }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(cargando = true, mensajeError = null) }

            try {
                val respuesta = RetrofitClient.divisaApi.obtenerTasas(divisaBase = divisaBase)
                val fechaFormateada = formatearFechaActual(respuesta.date)

                prefs.guardarTasas(respuesta.rates, fechaFormateada)
                prefs.guardarDivisaActiva(divisaBase)

                _uiState.update {
                    it.copy(
                        divisaActiva = respuesta.base,
                        tasas = respuesta.rates,
                        ultimaActualizacion = fechaFormateada,
                        cargando = false,
                        esOffline = false,
                        mensajeError = null
                    )
                }
            } catch (_: IOException) {
                val tasasEnCache = prefs.obtenerTasas()
                val fechaEnCache = prefs.obtenerUltimaActualizacion()

                _uiState.update {
                    it.copy(
                        cargando = false,
                        esOffline = true,
                        tasas = if (it.tasas.isNotEmpty()) it.tasas else tasasEnCache,
                        ultimaActualizacion = if (it.ultimaActualizacion.isNotBlank()) it.ultimaActualizacion else fechaEnCache,
                        mensajeError = if (tasasEnCache.isEmpty() && it.tasas.isEmpty()) {
                            "Sin conexión y sin datos previos guardados."
                        } else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeError = "Error: ${e.localizedMessage ?: "desconocido"}"
                    )
                }
            }
        }
    }

    /**
     * Convierte una moneda favorita en la divisa base activa (interacción Tap-to-Base).
     */
    fun seleccionarDivisaActiva(codigo: String) {
        if (_uiState.value.divisaActiva == codigo) return

        _uiState.update { estado ->
            val lista = estado.favoritos.toMutableList()
            if (!lista.contains(codigo)) {
                lista.add(0, codigo)
                prefs.guardarFavoritos(lista)
            }
            prefs.guardarDivisaActiva(codigo)
            estado.copy(divisaActiva = codigo, favoritos = lista)
        }
        cargarTasas(divisaBase = codigo)
    }

    /**
     * Actualiza el importe escrito por el usuario en el campo de texto.
     */
    fun onImporteCambiado(nuevoImporte: String) {
        val filtrado = nuevoImporte.replace(',', '.')
        if (filtrado.isEmpty() || filtrado.matches(Regex("^\\d*(\\.\\d{0,4})?$"))) {
            _uiState.update { it.copy(importeBase = filtrado) }
            prefs.guardarUltimoImporte(filtrado)
        }
    }

    /**
     * Alterna una divisa en la lista ordenada de favoritas.
     * Si se quita la divisa que actuaba como base activa de referencia,
     * la siguiente divisa favorita se convierte automáticamente en la nueva activa.
     */
    fun toggleFavorita(codigo: String) {
        var nuevaDivisaActivaParaCargar: String? = null

        _uiState.update { estado ->
            val lista = estado.favoritos.toMutableList()
            var activaActual = estado.divisaActiva

            if (lista.contains(codigo)) {
                // Quitar de favoritas (sin restricciones artificiales)
                lista.remove(codigo)

                // Si la divisa que se quita era la activa de referencia:
                if (codigo == activaActual) {
                    val siguienteActiva = lista.firstOrNull() ?: ""
                    activaActual = siguienteActiva
                    prefs.guardarDivisaActiva(activaActual)
                    if (activaActual.isNotEmpty()) {
                        nuevaDivisaActivaParaCargar = activaActual
                    }
                }
            } else {
                // Añadir a favoritas
                lista.add(codigo)

                // Si no había ninguna activa o no estaba en la lista:
                if (activaActual.isEmpty() || !lista.contains(activaActual)) {
                    activaActual = codigo
                    prefs.guardarDivisaActiva(activaActual)
                    nuevaDivisaActivaParaCargar = activaActual
                }
            }

            prefs.guardarFavoritos(lista)
            estado.copy(
                favoritos = lista,
                divisaActiva = activaActual
            )
        }

        nuevaDivisaActivaParaCargar?.let { nuevaBase ->
            cargarTasas(divisaBase = nuevaBase)
        }
    }

    /**
     * Desplaza una divisa favorita una posición hacia arriba en la lista.
     */
    fun moverFavoritaArriba(codigo: String) {
        _uiState.update { estado ->
            val lista = estado.favoritos.toMutableList()
            val indice = lista.indexOf(codigo)
            if (indice > 0) {
                Collections.swap(lista, indice, indice - 1)
                prefs.guardarFavoritos(lista)
                estado.copy(favoritos = lista)
            } else {
                estado
            }
        }
    }

    /**
     * Desplaza una divisa favorita una posición hacia abajo en la lista.
     */
    fun moverFavoritaAbajo(codigo: String) {
        _uiState.update { estado ->
            val lista = estado.favoritos.toMutableList()
            val indice = lista.indexOf(codigo)
            if (indice in 0 until lista.size - 1) {
                Collections.swap(lista, indice, indice + 1)
                prefs.guardarFavoritos(lista)
                estado.copy(favoritos = lista)
            } else {
                estado
            }
        }
    }

    /**
     * Obtiene el catálogo de divisas filtrado por la búsqueda actual.
     */
    fun obtenerCatalogoFiltrado(context: Context): List<DivisaInfo> {
        return DivisasCatalogo.filtrar(context, _uiState.value.queryBusqueda)
    }

    fun setQueryBusqueda(query: String) {
        _uiState.update { it.copy(queryBusqueda = query) }
    }

    fun setMostrarBottomSheetGestion(mostrar: Boolean) {
        _uiState.update {
            it.copy(
                mostrarBottomSheetGestion = mostrar,
                queryBusqueda = if (!mostrar) "" else it.queryBusqueda
            )
        }
    }

    fun setMostrarDialogoCreditos(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarDialogoCreditos = mostrar) }
    }

    fun refrescar() {
        if (_uiState.value.divisaActiva.isNotBlank()) {
            cargarTasas(divisaBase = _uiState.value.divisaActiva)
        }
    }

    private fun formatearFechaActual(fechaApi: String): String {
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaActual = formatoHora.format(Date())
        return "$fechaApi $horaActual CET"
    }
}
