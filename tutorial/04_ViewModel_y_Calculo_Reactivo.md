# 🔄 Paso 04: ViewModel y cálculo reactivo en tiempo real

En este paso conectaremos la capa de datos con la interfaz de usuario mediante un `ViewModel` reactivo. Diseñaremos un flujo de datos unidireccional (*Unidirectional Data Flow - UDF*) que gestione la divisa activa, el recálculo aritmético en memoria a 60 FPS y el funcionamiento sin conexión.

---

## 1. El estado de la pantalla: ConversorUiState.kt

En lugar de crear estados sellados que oculten la pantalla por completo ante una pérdida momentánea de cobertura, modelamos un estado de datos integral que mantiene las tasas previamente descargadas en memoria y refleja el estado de conexión mediante banderas booleanas (`esOffline`).

Crea el archivo `app/src/main/java/es/gestal/conversordivisas/ui/ConversorUiState.kt`:

```kotlin
package es.gestal.conversordivisas.ui

import es.gestal.conversordivisas.data.DivisaInfo
import es.gestal.conversordivisas.data.DivisasCatalogo

/**
 * Representa cada fila de divisa convertida que se pintará en la lista.
 *
 * @param info Información descriptiva de la divisa (código, nombre localizado, bandera emoji).
 * @param tasa Tipo de cambio unitario respecto a la divisa activa.
 * @param importeConvertido Resultado de multiplicar el importe base por la tasa.
 * @param esFavorita Indica si el usuario la tiene fijada en la pantalla principal.
 */
data class ItemDivisaConvertida(
    val info: DivisaInfo,
    val tasa: Double,
    val importeConvertido: Double,
    val esFavorita: Boolean
)

/**
 * Estado inmutable de la pantalla del conversor.
 * Soporta funcionamiento offline-first y orden personalizado de favoritos.
 */
data class ConversorUiState(
    val divisaActiva: String = "EUR",
    val importeBase: String = "100.00",
    val tasas: Map<String, Double> = emptyMap(),
    val favoritos: List<String> = listOf("EUR", "USD", "GBP", "JPY", "TRY", "CHF"),
    val cargando: Boolean = false,
    val esOffline: Boolean = false,
    val ultimaActualizacion: String = "",
    val mostrarBottomSheetGestion: Boolean = false,
    val mostrarDialogoCreditos: Boolean = false,
    val queryBusqueda: String = "",
    val mensajeError: String? = null
) {
    val tieneDivisas: Boolean
        get() = favoritos.isNotEmpty() && divisaActiva.isNotBlank()

    /**
     * Información completa de la divisa que actualmente actúa como base activa.
     */
    val infoDivisaActiva: DivisaInfo
        get() = if (divisaActiva.isNotBlank()) {
            DivisasCatalogo.buscarPorCodigo(divisaActiva)
        } else {
            DivisaInfo(codigo = "", nombreRes = es.gestal.conversordivisas.R.string.divisa_generica, simbolo = "", bandera = "🌐")
        }

    /**
     * Lista de divisas favoritas en el orden exacto configurado por el usuario
     * (excluyendo la divisa activa, que ya se muestra en la tarjeta Hero).
     */
    val favoritasConvertidas: List<ItemDivisaConvertida>
        get() {
            val importeNumerico = importeBase.toDoubleOrNull() ?: 0.0

            return favoritos
                .filter { it != divisaActiva }
                .map { codigo ->
                    val info = DivisasCatalogo.buscarPorCodigo(codigo)
                    val tasa = tasas[codigo] ?: 0.0
                    ItemDivisaConvertida(
                        info = info,
                        tasa = tasa,
                        importeConvertido = importeNumerico * tasa,
                        esFavorita = true
                    )
                }
        }
}
```

> [!IMPORTANT]
> **Inmutabilidad y recomposición en Jetpack Compose:**
> Jetpack Compose rastrea los cambios de estado mediante igualdad de instancias (`equals()`). Si mutáramos las propiedades de un objeto existente, Compose no detectaría la alteración y la interfaz no se redibujaría. Al definir `ConversorUiState` como una `data class` con atributos de solo lectura (`val`), obligamos a que cualquier cambio genere una nueva instancia mediante `.copy()`.
> 
> Además, la propiedad calculada `favoritasConvertidas` se recalcula de forma puramente matemática en memoria RAM en microsegundos, asegurando que escribir en el teclado produzca una respuesta inmediata a 60 / 120 FPS sin llamadas de red redundantes.

---

## 2. Implementación de ConversorViewModel.kt

El `ViewModel` extiende de `AndroidViewModel` para acceder al contexto de la aplicación e inicializar `PreferenciasDivisas`.

Sus responsabilidades son:
1. **Inicialización y recuperación de caché:** Cargar las divisas favoritas, la divisa activa y las tasas guardadas previamente.
2. **Descarga desde Frankfurter API:** Actualizar los tipos de cambio en segundo plano con `Dispatchers.IO`.
3. **Interacción «Tap-to-Base» (`seleccionarDivisaActiva`):** Al pulsar una divisa favorita, esta se convierte en la base activa y se descarga el tipo de cambio respecto a ella.
4. **Cálculo en memoria a 60 FPS (`onImporteCambiado`):** Cada pulsación numérica en el teclado actualiza `importeBase`. La multiplicación se realiza de forma inmediata en la propiedad calculada `favoritasConvertidas` sin lanzar peticiones de red.
5. **Reordenación de favoritas (`moverFavoritaArriba` y `moverFavoritaAbajo`):** Permite al usuario subir o bajar la prioridad de cada divisa en la lista y guarda el nuevo orden en SharedPreferences.
6. **Gestión de la hoja modal y créditos:** Apertura, cierre y búsqueda dinámica con soporte multilingüe.

Crea el archivo `app/src/main/java/es/gestal/conversordivisas/ui/ConversorViewModel.kt`:

```kotlin
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

    private val _uiState = MutableStateFlow(
        ConversorUiState(
            divisaActiva = prefs.obtenerDivisaActiva(),
            importeBase = prefs.obtenerUltimoImporte(),
            favoritos = prefs.obtenerFavoritos(),
            tasas = prefs.obtenerTasas(),
            ultimaActualizacion = prefs.obtenerUltimaActualizacion()
        )
    )
    val uiState: StateFlow<ConversorUiState> = _uiState.asStateFlow()

    init {
        // Carga inicial al arrancar la aplicación
        cargarTasas(divisaBase = _uiState.value.divisaActiva)
    }

    /**
     * Descarga las tasas de cambio de la API de Frankfurter con soporte offline-first.
     */
    fun cargarTasas(divisaBase: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(cargando = true, mensajeError = null) }

            try {
                val respuesta = RetrofitClient.divisaApi.obtenerTasas(divisaBase = divisaBase)
                val fechaFormateada = formatearFechaActual(respuesta.date)

                // Guardamos en caché persistente para soporte sin conexión
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
                // Fallo de conexión: recurrimos a la caché persistente
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
     * Si se quita de favoritas la divisa que actuaba como base activa de referencia,
     * la siguiente divisa favorita se convierte automáticamente en la nueva base activa.
     */
    fun toggleFavorita(codigo: String) {
        var nuevaDivisaActivaParaCargar: String? = null

        _uiState.update { estado ->
            val lista = estado.favoritos.toMutableList()
            var activaActual = estado.divisaActiva

            if (lista.contains(codigo)) {
                // Quitar de favoritas sin límites artificiales
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
     * Obtiene el catálogo de divisas filtrado por la búsqueda actual y el idioma activo.
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
        cargarTasas(divisaBase = _uiState.value.divisaActiva)
    }

    private fun formatearFechaActual(fechaApi: String): String {
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaActual = formatoHora.format(Date())
        return "$fechaApi $horaActual CET"
    }
}
```

---

## 3. Claves de la arquitectura implementada

1. **Eficiencia en batería y red:** No se dispara ninguna petición HTTP cuando el usuario teclea números en el importe. La multiplicación aritmética se realiza instantáneamente en memoria.
2. **Actualización atómica del estado:** El uso de `_uiState.update { ... }` previene condiciones de carrera en corrutinas concurrentes.
3. **Resiliencia ante la desconexión:** Si el usuario abre la app en el avión o en el metro, la aplicación arranca con las últimas tasas disponibles y permite operar sin interrupciones.

> [!TIP]
> **Concurrencia segura con `MutableStateFlow.update`:**
> En aplicaciones móviles reales, los eventos de usuario (toques en la pantalla) y las respuestas de red (corrutinas asíncronas) compiten frecuentemente por el estado. Asignar directamente `_uiState.value = nuevoEstado` es vulnerable a condiciones de carrera si dos hilos leen el mismo valor simultáneamente. `_uiState.update { ... }` ejecuta un bucle de comparación y cambio atómico (*Compare-And-Set* o CAS), garantizando que ninguna actualización se pierda.

> [!IMPORTANT]
> **Manejo del caso borde: Desmarcar la divisa activa de referencia:**
> Un error común de usabilidad en conversores de divisas ocurre cuando el usuario quita de favoritas la moneda que en ese momento encabeza la pantalla.
> En nuestro `ViewModel`, `toggleFavorita()` detecta esta situación (`if (codigo == activaActual)`), promueve automáticamente a la primera divisa favorita restante (`lista.firstOrNull()`) como nueva moneda activa y lanza la descarga de sus tipos de cambio correspondientes. Si la lista queda vacía, `tieneDivisas` pasa a `false`, permitiendo a Compose conmutar suavemente a la pantalla vacía de bienvenida.

---

## 4. Resumen del paso

* Diseñamos un estado inmutable `ConversorUiState` con cálculo dinámico de divisas favoritas.
* Implementamos la acción *Tap-to-Base* para invertir la moneda activa con un solo toque.
* Dotamos a la aplicación de resiliencia sin conexión mediante persistencia local de tasas.

En el **[Paso 05](05_Interfaz_Grafica_Jetpack_Compose.md)** desarrollaremos los componentes visuales en Jetpack Compose con Material 3.
