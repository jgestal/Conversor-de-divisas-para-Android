package es.gestal.conversordivisas.data

import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName

/**
 * =================================================================================================
 * MODELOS DE LA CAPA DE DATOS (Data Layer)
 * =================================================================================================
 * Este archivo centraliza los modelos de transferencia de datos (DTO) devueltos por la API de red
 * y las entidades de dominio que describen cada divisa dentro de la aplicación.
 */

/**
 * Objeto de transferencia de datos (DTO) que mapea la respuesta JSON devuelta por la API de Frankfurter.
 *
 * Ejemplo de respuesta devuelta por el servidor:
 * {
 *   "amount": 1.0,
 *   "base": "EUR",
 *   "date": "2026-10-14",
 *   "rates": { "USD": 1.085, "GBP": 0.854, "JPY": 162.3 }
 * }
 */
data class CambioRespuesta(
    // Importe base sobre el que se calculan las tasas de cambio (habitualmente 1.0)
    @SerializedName("amount") 
    val amount: Double,

    // Código ISO 4217 de la divisa de referencia utilizada en la petición (ej. "EUR")
    @SerializedName("base") 
    val base: String,

    // Fecha oficial de fijación de tipos de cambio por el Banco Central Europeo (formato YYYY-MM-DD)
    @SerializedName("date") 
    val date: String,

    // Mapa dinámico de tasas de cambio relativas a la divisa base:
    // Clave = Código de divisa (ej. "USD"), Valor = Tipo de cambio numérico (ej. 1.085).
    // Usar Map<String, Double> permite soportar cualquier cantidad de divisas sin crear propiedades fijas.
    @SerializedName("rates") 
    val rates: Map<String, Double>
)

/**
 * Modelo de dominio que encapsula los metadatos visuales y descriptivos de una divisa.
 *
 * Diseñado bajo principios de Clean Architecture: es una entidad inmutable pura en Kotlin
 * sin acoplamiento a Context ni dependencias del ciclo de vida de Android.
 */
data class DivisaInfo(
    // Código estándar internacional ISO 4217 de 3 caracteres mayúsculos (ej. "EUR", "USD", "JPY")
    val codigo: String,

    // Identificador entero del recurso de cadena en strings.xml (ej. R.string.divisa_eur).
    // La anotación @get:StringRes instruye al compilador y al linter a comprobar que
    // esta propiedad reciba estrictamente referencias válidas de tipo R.string.*.
    @get:StringRes 
    val nombreRes: Int,

    // Símbolo gráfico tradicional utilizado para formatear importes en pantalla (ej. "€", "$", "£", "₺")
    val simbolo: String,

    // Representación gráfica de la bandera del país emisor mediante un emoji nativo de Unicode.
    // Ventaja: No consume megabytes en imágenes PNG/SVG y se renderiza con aceleración de hardware.
    val bandera: String
)

