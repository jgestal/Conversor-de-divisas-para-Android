package es.gestal.conversordivisas.data

import android.content.Context
import es.gestal.conversordivisas.R

/**
 * Catálogo estático de metadatos de divisas mundiales
 * con soporte de localización multi-idioma y banderas emoji.
 */
object DivisasCatalogo {

    val todas: List<DivisaInfo> = listOf(
        DivisaInfo("EUR", R.string.divisa_eur, "€", "🇪🇺"),
        DivisaInfo("USD", R.string.divisa_usd, "$", "🇺🇸"),
        DivisaInfo("GBP", R.string.divisa_gbp, "£", "🇬🇧"),
        DivisaInfo("TRY", R.string.divisa_try, "₺", "🇹🇷"),
        DivisaInfo("JPY", R.string.divisa_jpy, "¥", "🇯🇵"),
        DivisaInfo("CHF", R.string.divisa_chf, "CHF", "🇨🇭"),
        DivisaInfo("CAD", R.string.divisa_cad, "CA$", "🇨🇦"),
        DivisaInfo("AUD", R.string.divisa_aud, "A$", "🇦🇺"),
        DivisaInfo("CNY", R.string.divisa_cny, "¥", "🇨🇳"),
        DivisaInfo("INR", R.string.divisa_inr, "₹", "🇮🇳"),
        DivisaInfo("BRL", R.string.divisa_brl, "R$", "🇧🇷"),
        DivisaInfo("MXN", R.string.divisa_mxn, "$", "🇲🇽"),
        DivisaInfo("SEK", R.string.divisa_sek, "kr", "🇸🇪"),
        DivisaInfo("NOK", R.string.divisa_nok, "kr", "🇳🇴"),
        DivisaInfo("DKK", R.string.divisa_dkk, "kr", "🇩🇰"),
        DivisaInfo("PLN", R.string.divisa_pln, "zł", "🇵🇱"),
        DivisaInfo("CZK", R.string.divisa_czk, "Kč", "🇨🇿"),
        DivisaInfo("HUF", R.string.divisa_huf, "Ft", "🇭🇺"),
        DivisaInfo("ILS", R.string.divisa_ils, "₪", "🇮🇱"),
        DivisaInfo("KRW", R.string.divisa_krw, "₩", "🇰🇷"),
        DivisaInfo("SGD", R.string.divisa_sgd, "S$", "🇸🇬"),
        DivisaInfo("NZD", R.string.divisa_nzd, "NZ$", "🇳🇿"),
        DivisaInfo("THB", R.string.divisa_thb, "฿", "🇹🇭"),
        DivisaInfo("ZAR", R.string.divisa_zar, "R", "🇿🇦")
    )

    private val mapaPorCodigo: Map<String, DivisaInfo> = todas.associateBy { it.codigo }

    fun buscarPorCodigo(codigo: String): DivisaInfo {
        return mapaPorCodigo[codigo] ?: DivisaInfo(
            codigo = codigo,
            nombreRes = R.string.divisa_generica,
            simbolo = codigo,
            bandera = "🌐"
        )
    }

    /**
     * Filtra el catálogo en tiempo real según el código o el nombre localizado.
     */
    fun filtrar(contexto: Context, texto: String): List<DivisaInfo> {
        if (texto.isBlank()) return todas
        val query = texto.trim().lowercase()
        return todas.filter {
            it.codigo.lowercase().contains(query) ||
                    contexto.getString(it.nombreRes).lowercase().contains(query)
        }
    }
}
