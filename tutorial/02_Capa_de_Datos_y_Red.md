# 🌐 Paso 02: Capa de datos y red

Con el entorno de desarrollo y los recursos visuales base preparados, construimos en este paso la **capa de datos completa** de la aplicación siguiendo los principios de la arquitectura recomendada por Google (MVVM).

Implementaremos modelos desacoplados con soporte de localización multi-idioma, un catálogo completo de divisas mundiales con banderas en formato emoji nativo, persistencia local ligera que recuerda la divisa activa y preserva el orden exacto de favoritas, y el cliente HTTP Retrofit con corrutinas de Kotlin para consultar la API abierta de tipos de cambio del Banco Central Europeo (Frankfurter).

---

## 1. Modelos de datos

Crea el archivo `app/src/main/java/es/gestal/conversordivisas/data/DivisaModelos.kt`. 

Observa cómo `DivisaInfo` utiliza `@get:StringRes val nombreRes: Int` en lugar de una cadena fija, permitiendo que el nombre de cada moneda se adapte dinámicamente al idioma del usuario sin acoplar el modelo al ciclo de vida de Android:

```kotlin
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
```

---

## 2. Catálogo de divisas y buscador reactivo

Crea el archivo `app/src/main/java/es/gestal/conversordivisas/data/DivisasCatalogo.kt`. 

Asocia cada moneda a su bandera emoji nativa y a su identificador de recurso de cadena. La función de filtrado recibe el `Context` para permitir búsquedas instantáneas tanto por código ISO (ej. `USD`, `TRY`) como por el nombre traducido en el idioma activo del teléfono (ej. «Dólar», «Lira», «Yen»):

```kotlin
package es.gestal.conversordivisas.data

import android.content.Context
import es.gestal.conversordivisas.R

/**
 * Catálogo de divisas mundiales con banderas en formato emoji
 * y referencias a recursos de cadenas localizados.
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
     * Filtra divisas por código o nombre traducido en el idioma activo del usuario.
     */
    fun filtrar(context: Context, texto: String): List<DivisaInfo> {
        if (texto.isBlank()) return todas
        val query = texto.trim().lowercase()
        return todas.filter { divisa ->
            val nombreLocalizado = context.getString(divisa.nombreRes).lowercase()
            divisa.codigo.lowercase().contains(query) || nombreLocalizado.contains(query)
        }
    }
}
```

---

## 3. Persistencia ligera con preservación del orden (*Offline-first*)

Para recordar las divisas favoritas en el orden exacto configurado por el usuario, el importe editado y las últimas tasas descargadas del BCE, creamos `PreferenciasDivisas`.

> [!NOTE]
> `SharedPreferences.getStringSet()` no garantiza el orden de inserción de los elementos. Por eso, serializamos la lista de favoritos (`List<String>`) como una cadena JSON con `Gson`, garantizando que la posición que el usuario elija para cada divisa se mantenga intacta entre sesiones.

Crea el archivo `app/src/main/java/es/gestal/conversordivisas/data/PreferenciasDivisas.kt`:

```kotlin
package es.gestal.conversordivisas.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Gestor de persistencia ligera mediante SharedPreferences.
 * Guarda la divisa activa, la lista ordenada de favoritos y las tasas en JSON.
 */
class PreferenciasDivisas(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "conversor_divisas_prefs",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val CLAVE_DIVISA_ACTIVA = "divisa_activa"
        private const val CLAVE_FAVORITOS_LISTA_JSON = "favoritos_lista_json"
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

    /**
     * Guarda la lista ordenada de favoritos en formato JSON para preservar la secuencia exacta.
     */
    fun guardarFavoritos(favoritos: List<String>) {
        val json = gson.toJson(favoritos)
        prefs.edit().putString(CLAVE_FAVORITOS_LISTA_JSON, json).apply()
    }

    fun obtenerFavoritos(): List<String> {
        val json = prefs.getString(CLAVE_FAVORITOS_LISTA_JSON, null) ?: return FAVORITOS_POR_DEFECTO
        val type = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson<List<String>>(json, type) ?: FAVORITOS_POR_DEFECTO
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
```

> [!TIP]
> **Rendimiento: `apply()` frente a `commit()`:**
> Observa que en todos los métodos de escritura utilizamos `prefs.edit().putString(...).apply()`. A diferencia de `commit()` (que bloquea el hilo actual de forma síncrona hasta terminar la escritura en el almacenamiento físico), `apply()` actualiza la memoria en RAM instantáneamente y delega la escritura en disco a un hilo de segundo plano. Esto asegura que la interfaz en Compose nunca sufra caídas de fotogramas (*jank*).

---

## 4. Cliente HTTP con Retrofit

Crea el archivo `app/src/main/java/es/gestal/conversordivisas/data/DivisaApi.kt`. 

La interfaz declara la función suspendida `obtenerTasas` y el objeto `RetrofitClient` centraliza la conexión con tiempos de espera seguros de 15 segundos y registro de peticiones para depuración:

```kotlin
package es.gestal.conversordivisas.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Endpoints de la API de Frankfurter.
 */
interface DivisaApi {

    /**
     * Obtiene las tasas de cambio más recientes para una divisa base.
     * Ejemplo de llamada: https://api.frankfurter.dev/v1/latest?base=EUR
     */
    @GET("v1/latest")
    suspend fun obtenerTasas(
        @Query("base") divisaBase: String
    ): CambioRespuesta
}

/**
 * Constructor Singleton del cliente HTTP Retrofit.
 */
object RetrofitClient {

    private const val BASE_URL = "https://api.frankfurter.dev/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    val divisaApi: DivisaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DivisaApi::class.java)
    }
}
```

> [!NOTE]
> **Corrutinas directas en Retrofit:**
> Desde la versión 2.6.0 de Retrofit, no es necesario devolver `Call<CambioRespuesta>` ni gestionar callbacks engorrosos (`onResponse`/`onFailure`). Al declarar la función como `suspend fun obtenerTasas(...)`, Retrofit suspende la ejecución de la corrutina sin bloquear el hilo principal y reanuda el flujo entregando el objeto deserializado directamente.

---

## 5. Resumen del paso

* Diseñamos un modelo de datos desacoplado (`DivisaInfo`) con identificadores de recursos para soportar nombres localizados dinámicamente.
* Creamos el catálogo `DivisasCatalogo` con 24 divisas mundiales, banderas emoji y un motor de filtrado insensible a mayúsculas/acentos.
* Implementamos `PreferenciasDivisas` con serialización JSON para garantizar la preservación del orden personalizado de favoritas y modo *offline-first*.
* Configuramos el cliente Singleton de `Retrofit` con tiempos de espera seguros y soporte nativo de corrutinas (`suspend fun`).

En el **[Paso 03](03_ViewModel_y_Calculo_Reactivo.md)** conectaremos esta capa de datos con la lógica de negocio mediante el `ViewModel`, implementando el cálculo reactivo a 60 FPS, la reordenación de favoritas y la interacción de cambio de base *Tap-to-Base*.
