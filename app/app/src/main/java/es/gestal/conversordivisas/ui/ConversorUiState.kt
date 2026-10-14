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
