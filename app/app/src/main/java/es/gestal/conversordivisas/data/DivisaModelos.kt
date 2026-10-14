package es.gestal.conversordivisas.data

import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName

/**
 * Modelo que mapea la respuesta devuelta por la API de Frankfurter.
 */
data class CambioRespuesta(
    @SerializedName("amount") val amount: Double,
    @SerializedName("base") val base: String,
    @SerializedName("date") val date: String,
    @SerializedName("rates") val rates: Map<String, Double>
)

/**
 * Metadatos descriptivos de cada divisa con soporte de localización multi-idioma.
 *
 * @param codigo Código ISO 4217 de 3 letras (ej. EUR, TRY, USD).
 * @param nombreRes Recurso de texto para el nombre localizado en el idioma del sistema.
 * @param simbolo Símbolo gráfico usual (ej. €, ₺, $, £).
 * @param bandera Emoji de la bandera del país emisor (ej. 🇪🇺, 🇹🇷, 🇺🇸).
 */
data class DivisaInfo(
    val codigo: String,
    @get:StringRes val nombreRes: Int,
    val simbolo: String,
    val bandera: String
)
