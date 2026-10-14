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
