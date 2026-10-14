package es.gestal.conversordivisas.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Gestor de persistencia ligera mediante SharedPreferences.
 * Guarda y recupera la lista ordenada de favoritos, la divisa activa
 * y las últimas tasas descargadas para el modo sin conexión.
 */
class PreferenciasDivisas(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "conversor_divisas_prefs",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val CLAVE_DIVISA_ACTIVA = "divisa_activa"
        private const val CLAVE_FAVORITOS_LISTA = "favoritos_lista_json"
        private const val CLAVE_TASAS_JSON = "tasas_json"
        private const val CLAVE_ULTIMA_ACTUALIZACION = "ultima_actualizacion"
        private const val CLAVE_IMPORTE = "ultimo_importe"

        val FAVORITOS_POR_DEFECTO = listOf("EUR", "USD", "GBP", "JPY", "TRY", "CHF")
    }

    fun guardarDivisaActiva(codigo: String) {
        prefs.edit().putString(CLAVE_DIVISA_ACTIVA, codigo).apply()
    }

    fun obtenerDivisaActiva(): String {
        return prefs.getString(CLAVE_DIVISA_ACTIVA, "EUR") ?: "EUR"
    }

    fun guardarFavoritos(favoritos: List<String>) {
        val json = gson.toJson(favoritos)
        prefs.edit().putString(CLAVE_FAVORITOS_LISTA, json).apply()
    }

    fun obtenerFavoritos(): List<String> {
        val json = prefs.getString(CLAVE_FAVORITOS_LISTA, null) ?: return FAVORITOS_POR_DEFECTO
        val type = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(json, type) ?: FAVORITOS_POR_DEFECTO
        } catch (_: Exception) {
            FAVORITOS_POR_DEFECTO
        }
    }

    fun guardarUltimoImporte(importe: String) {
        prefs.edit().putString(CLAVE_IMPORTE, importe).apply()
    }

    fun obtenerUltimoImporte(): String {
        return prefs.getString(CLAVE_IMPORTE, "100.00") ?: "100.00"
    }

    fun guardarTasas(tasas: Map<String, Double>, fechaSincronizacion: String) {
        val json = gson.toJson(tasas)
        prefs.edit()
            .putString(CLAVE_TASAS_JSON, json)
            .putString(CLAVE_ULTIMA_ACTUALIZACION, fechaSincronizacion)
            .apply()
    }

    fun obtenerTasas(): Map<String, Double> {
        val json = prefs.getString(CLAVE_TASAS_JSON, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, Double>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun obtenerUltimaActualizacion(): String {
        return prefs.getString(CLAVE_ULTIMA_ACTUALIZACION, "") ?: ""
    }
}
