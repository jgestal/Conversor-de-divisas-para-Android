# 🌐 Paso 01: Configuración del proyecto y capa de red

En este paso configuraremos el proyecto en Android Studio con Jetpack Compose, añadiremos las dependencias necesarias y construiremos la capa de datos completa: modelos, catálogo con banderas emoji, persistencia local ligera y el cliente HTTP Retrofit.

---

## 1. Creación del proyecto en Android Studio

1. Abre Android Studio y pulsa en **New Project**.
2. Selecciona la plantilla **Empty Activity** (con el logotipo de Jetpack Compose).
3. Configura los parámetros básicos del proyecto:
   * **Name:** `Conversor de divisas`
   * **Package name:** `es.gestal.conversordivisas` (o tu propio identificador personalizado, por ejemplo `com.tu_nombre.conversordivisas`).
   * **Language:** Kotlin
   * **Minimum SDK:** API 26 (Android 8.0 Oreo) o superior.
   * **Build configuration language:** Kotlin DSL (`build.gradle.kts`).

---

## 2. Dependencias y configuración en `app/build.gradle.kts` (Nivel Módulo `:app`)

> [!NOTE]
> En la vista **Android** del panel izquierdo de Android Studio verás dos archivos con el mismo nombre dentro de la sección **Gradle Scripts**:
> * `build.gradle.kts (Project: Conversor_de_divisas)`: Configuración global de plugins y repositorios a nivel raíz.
> * `build.gradle.kts (Module :app)`: Configuración del módulo de la aplicación (`app/build.gradle.kts`).
> 
> Debes abrir el archivo a **nivel de módulo** (`build.gradle.kts (Module :app)`).

> [!IMPORTANT]
> **Identificador de paquete (`namespace` y `applicationId`):**
> En este tutorial utilizamos `es.gestal.conversordivisas`, que corresponde al dominio y autoría personal de **Juan Gestal** (`gestal.es`).
> Al crear tu propia aplicación debes indicar **tu propio espacio de nombres** en formato de dominio invertido (por ejemplo: `com.tu_nombre.conversordivisas` o `dev.tu_usuario.conversordivisas`). Ten presente que una vez que subas la aplicación a Google Play Store, el `applicationId` no se podrá volver a cambiar.

Abre el archivo `app/build.gradle.kts` (Nivel Módulo `:app`) e incorpora las dependencias para la arquitectura, la capa de red y los componentes visuales:

```kotlin
// =========================================================================================
// 1. PLUGINS DE GRADLE
// =========================================================================================
plugins {
    // Plugin principal para compilar aplicaciones Android
    alias(libs.plugins.android.application)
    // Plugin oficial de Jetpack Compose para optimizar y transformar funciones @Composable
    alias(libs.plugins.kotlin.compose)
}

// =========================================================================================
// 2. CONFIGURACIÓN DEL SISTEMA ANDROID
// =========================================================================================
android {
    // Espacio de nombres base para la generación de la clase R y BuildConfig.
    // Cambia 'es.gestal.conversordivisas' por tu propio paquete (ej. com.tu_nombre.conversordivisas)
    namespace = "es.gestal.conversordivisas"

    // Versión del SDK de Android contra la que se compila el código fuente
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // Identificador universal y único de la app en Google Play Store y en el dispositivo
        applicationId = "es.gestal.conversordivisas"

        // Versión mínima de Android requerida (API 26 = Android 8.0 Oreo).
        // Cubre más del 95% de dispositivos activos y soporta APIs modernas de red y fecha.
        minSdk = 26

        // Versión del sistema operativo para la que se ha probado y optimizado el comportamiento
        targetSdk = 37

        // Número entero correlativo para control interno de actualizaciones en Google Play (1, 2, 3...)
        versionCode = 1

        // Cadena de texto legible de la versión mostrada a los usuarios en la ficha de la tienda
        versionName = "1.0"

        // Ejecutor oficial de pruebas instrumentadas en emulador o dispositivo real
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // En desarrollo se desactiva la ofuscación R8 para agilizar los tiempos de compilación
            optimization {
                enable = false
            }
        }
    }

    // Nivel de compatibilidad del bytecode de Java (Java 11)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Activa el soporte nativo para compilar interfaces con Jetpack Compose
    buildFeatures {
        compose = true
    }
}

// =========================================================================================
// 3. DEPENDENCIAS EXTERNAS
// =========================================================================================
dependencies {
    // Compose Bill of Materials (BOM): Alinea automáticamente las versiones compatibles de Compose
    implementation(platform(libs.androidx.compose.bom))

    // Integración de Compose con la Activity y ciclo de vida del sistema
    implementation(libs.androidx.activity.compose)

    // Componentes de diseño moderno Material Design 3 (Cards, TextFields, TopAppBar, BottomSheet)
    implementation(libs.androidx.compose.material3)

    // Núcleo de dibujo, geometría y gráficos de Jetpack Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Extensiones KTX para el núcleo de Android y tiempo de ejecución
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // API oficial de Splash Screen de Android (garantiza compatibilidad visual desde Android 12+)
    implementation("androidx.core:core-splashscreen:1.0.1")

    // ViewModel para Compose: Permite instanciar y vincular viewModels reactivos con viewModel()
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Catálogo ampliado de iconos de Material (Star, StarOutline, Clear, Search, Sync, SwapVert...)
    implementation("androidx.compose.material:material-icons-extended")

    // Capa de red HTTP con Retrofit, convertidor Gson y registrador de llamadas OkHttp
    val retrofitVersion = "2.11.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Corrutinas de Kotlin para operaciones asíncronas no bloqueantes en hilos secundarios (IO)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
```

Sincroniza el proyecto pulsando en **Sync Now** en la barra superior de Android Studio.

---

## 3. Permiso de acceso a internet en AndroidManifest.xml

Para consultar la API pública de Frankfurter necesitamos declarar el permiso de acceso a internet en `app/src/main/AndroidManifest.xml`. 

Al haber creado el proyecto con el asistente de Android Studio, ya dispones de un archivo `AndroidManifest.xml` funcional configurado con tu actividad principal y el tema por defecto del sistema. En este punto, **lo único que necesitamos incorporar es la línea con el permiso de internet** (`<uses-permission android:name="android.permission.INTERNET" />`) antes de la etiqueta `<application>`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- 
  Manifiesto raíz de la aplicación. 
  Declara los permisos del sistema, metadatos globales y los componentes (Activities, Services, etc.)
  para que el sistema operativo Android sepa cómo gestionar la aplicación.
-->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- ===================================================================================== -->
    <!-- 1. PERMISOS DEL SISTEMA OPERATIVO                                                     -->
    <!-- ===================================================================================== -->
    <!-- Permiso obligatorio para abrir sockets y realizar peticiones HTTP/HTTPS con Retrofit -->
    <uses-permission android:name="android.permission.INTERNET" />

    <!-- ===================================================================================== -->
    <!-- 2. CONFIGURACIÓN GLOBAL DE LA APLICACIÓN                                              -->
    <!-- ===================================================================================== -->
    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">

        <!-- ================================================================================= -->
        <!-- 3. ACTIVIDAD PRINCIPAL (MainActivity)                                             -->
        <!-- ================================================================================= -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@android:style/Theme.Material.Light.NoActionBar">

            <!-- 
              Filtro de intenciones (Intent Filter):
              Le indica a Android que esta Activity es la pantalla de arranque
              y que debe colocar su icono en el lanzador del sistema (Launcher).
            -->
            <intent-filter>
                <!-- Define esta actividad como el punto de entrada principal (Main) -->
                <action android:name="android.intent.action.MAIN" />

                <!-- Registra la aplicación en el escritorio y cajón de apps del teléfono -->
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

> [!NOTE]
> **Permisos normales frente a peligrosos:** En Android, `android.permission.INTERNET` está catalogado como permiso *normal*. No requiere solicitar autorización en tiempo de ejecución al usuario mediante cuadros de diálogo, pero es estrictamente obligatorio declararlo en el manifiesto. Si se omite, cualquier petición de Retrofit arrojará un `SecurityException`.

---

### 3.1. Recursos visuales y tema del Splash Screen

A partir de Android 12, el sistema operativo muestra obligatoriamente una pantalla de presentación al arrancar en frío. La librería oficial `androidx.core:core-splashscreen` retrocompatibiliza este estándar en todas las versiones de Android desde API 21+.

Para evitar avisos de recursos inexistentes en el entorno de desarrollo, crearemos primero los elementos visuales base (color e icono vectorial) y después definiremos el estilo en `themes.xml`:

#### 1. Color de fondo del Splash (`res/values/colors.xml`)

Abre o crea el archivo `app/src/main/res/values/colors.xml` y define el color oscuro de fondo para la pantalla de arranque:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="splash_background">#0F172A</color>
</resources>
```

#### 2. Icono central del Splash Screen (`ic_splash_icon.xml`)

Crea el archivo vectorial `app/src/main/res/drawable/ic_splash_icon.xml` con un diseño que combina las flechas de intercambio y las monedas de Euro y Dólar:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="160dp"
    android:height="160dp"
    android:viewportWidth="512"
    android:viewportHeight="512">

    <!-- Círculo contenedor con resplandor -->
    <path
        android:fillColor="#172554"
        android:pathData="M 256,256 m -200,0 a 200,200 0 1,1 400,0 a 200,200 0 1,1 -400,0" />

    <group
        android:pivotX="256"
        android:pivotY="256"
        android:scaleX="0.82"
        android:scaleY="0.82">

        <!-- Flecha superior dorada -->
        <path
            android:strokeColor="#F59E0B"
            android:strokeWidth="24"
            android:strokeLineCap="round"
            android:pathData="M 152,236 C 145,170 196,112 268,112 C 322,112 368,144 388,190" />
        <path
            android:fillColor="#F59E0B"
            android:pathData="M 374,206 L 406,186 L 396,148 Z" />

        <!-- Flecha inferior cian -->
        <path
            android:strokeColor="#0EA5E9"
            android:strokeWidth="24"
            android:strokeLineCap="round"
            android:pathData="M 360,276 C 367,342 316,400 244,400 C 190,400 144,368 124,322" />
        <path
            android:fillColor="#0EA5E9"
            android:pathData="M 138,306 L 106,326 L 116,364 Z" />

        <!-- Moneda Euro -->
        <path
            android:fillColor="#F59E0B"
            android:pathData="M 196,204 m -62,0 a 62,62 0 1,1 124,0 a 62,62 0 1,1 -124,0" />

        <!-- Moneda Dólar -->
        <path
            android:fillColor="#0EA5E9"
            android:pathData="M 316,308 m -62,0 a 62,62 0 1,1 124,0 a 62,62 0 1,1 -124,0" />
    </group>
</vector>
```

#### 3. Configuración del tema en `res/values/themes.xml`

Ahora que el color (`@color/splash_background`) y el icono (`@drawable/ic_splash_icon`) ya existen en el proyecto, abre `app/src/main/res/values/themes.xml` y define el tema de Splash Screen junto con el tema base de la aplicación:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Tema de inicio compatible con la API oficial de Splash Screen -->
    <style name="Theme.App.Starting" parent="Theme.SplashScreen">
        <!-- Color de fondo oscuro de arranque -->
        <item name="windowSplashScreenBackground">@color/splash_background</item>
        <!-- Icono vectorial animado central -->
        <item name="windowSplashScreenAnimatedIcon">@drawable/ic_splash_icon</item>
        <!-- Tema principal al que transicionar tras ocultar el splash -->
        <item name="postSplashScreenTheme">@style/Theme.ConversorDivisas</item>
    </style>

    <!-- Tema base de la aplicación tras finalizar el splash screen -->
    <style name="Theme.ConversorDivisas" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
    </style>
</resources>
```

> [!IMPORTANT]
> **¿Por qué usar postSplashScreenTheme?**
> La propiedad `postSplashScreenTheme` le indica al sistema operativo a qué estilo visual debe conmutar la ventana una vez que la primera composición de Jetpack Compose está lista para dibujarse. Esto elimina el indeseable «parpadeo blanco» (*white flash*) en los arranques en frío.

---

### 3.2. Vinculación del Splash Screen en AndroidManifest.xml

Una vez que el estilo `@style/Theme.App.Starting` está definido en `themes.xml`, **volvemos a `app/src/main/AndroidManifest.xml`** para aplicarlo como tema de arranque tanto a nivel global de la aplicación (`<application>`) como en la actividad principal (`<activity>`):

```xml
    <application
        ...
        android:theme="@style/Theme.App.Starting">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.App.Starting">
            ...
        </activity>
    </application>
```

Siguiendo este orden secuencial, Android Studio autocompletará `@style/Theme.App.Starting` inmediatamente sin arrojar ningún aviso en rojo, garantizando una experiencia de desarrollo limpia y sin bloqueos visuales.

---

## 4. Internacionalización y catálogo de cadenas (i18n)

Para lograr el máximo éxito en Google Play Store, una aplicación debe hablar la lengua de sus usuarios. Android proporciona un sistema de localización automático basado en carpetas de recursos (`res/values/strings.xml` para el idioma por defecto, y `res/values-<idioma>/strings.xml` para cada localización específica).

Cuando un usuario abre la aplicación, el sistema operativo inspecciona la configuración regional de su dispositivo y selecciona de forma automática el archivo correspondiente. Si el idioma del teléfono no dispone de traducción explícita, Android recurre al catálogo por defecto (`res/values/strings.xml`), que configuramos en **inglés** por ser la lengua vehicular internacional.

### 4.1. Catálogo por defecto en inglés (`app/src/main/res/values/strings.xml`)

```xml
<resources>
    <string name="app_name">Currency Converter</string>
    <string name="divisa_activa">Active currency</string>
    <string name="sin_conexion">Offline (saved rates)</string>
    <string name="bce_sincronizado">ECB: %1$s</string>
    <string name="favoritas_titulo">Favorites (tap to activate)</string>
    <string name="gestionar">Manage</string>
    <string name="gestionar_divisas_titulo">Manage currencies</string>
    <string name="buscar_divisa_placeholder">Search by name or code (e.g. TRY, Dollar)...</string>
    <string name="activa_tag">ACTIVE</string>
    <string name="quitar_de_favoritas">Remove from favorites</string>
    <string name="anadir_a_favoritas">Add to favorites</string>
    <string name="toca_para_activar">Tap to activate</string>
    <string name="mover_arriba">Move up</string>
    <string name="mover_abajo">Move down</string>
    <string name="actualizar_tasas">Update exchange rates</string>
    <string name="creditos_y_autor">Credits and author</string>
    <string name="creditos_titulo">Currency Converter</string>
    <string name="desarrollado_por">Developed by Juan Gestal</string>
    <string name="descripcion_creditos">Open source application distributed under the MIT license. Designed for educational purposes to learn Jetpack Compose and API integration in Android.</string>
    <string name="fuente_datos_creditos">Official reference exchange rates from the European Central Bank synchronized via the open Frankfurter API.</string>
    <string name="cerrar">Close</string>

    <!-- Nombres de divisas -->
    <string name="divisa_eur">Euro</string>
    <string name="divisa_usd">US Dollar</string>
    <string name="divisa_gbp">British Pound</string>
    <string name="divisa_try">Turkish Lira</string>
    <string name="divisa_jpy">Japanese Yen</string>
    <string name="divisa_chf">Swiss Franc</string>
    <!-- Resto de divisas con clave prefijada divisa_* -->
</resources>
```

### 4.2. Localización en español (`app/src/main/res/values-es/strings.xml`)

Para añadir un idioma (en este ejemplo, el español), basta con crear el directorio `res/values-es/` y proporcionar el archivo `strings.xml` con exactamente las mismas claves traducidas:

```xml
<resources>
    <string name="app_name">Conversor de divisas</string>
    <string name="divisa_activa">Divisa activa</string>
    <string name="sin_conexion">Sin conexión (datos guardados)</string>
    <string name="bce_sincronizado">BCE: %1$s</string>
    <string name="favoritas_titulo">Favoritas (toca una para activarla)</string>
    <string name="gestionar">Gestionar</string>
    <string name="gestionar_divisas_titulo">Gestionar divisas</string>
    <string name="buscar_divisa_placeholder">Buscar por nombre o código (ej. TRY, Lira)...</string>
    <string name="activa_tag">ACTIVA</string>
    <string name="quitar_de_favoritas">Quitar de favoritas</string>
    <string name="anadir_a_favoritas">Añadir a favoritas</string>
    <string name="toca_para_activar">Toca para activar</string>
    <string name="mover_arriba">Mover arriba</string>
    <string name="mover_abajo">Mover abajo</string>
    <string name="actualizar_tasas">Actualizar tasas</string>
    <string name="creditos_y_autor">Créditos y autor</string>
    <string name="creditos_titulo">Conversor de divisas</string>
    <string name="desarrollado_por">Desarrollado por Juan Gestal</string>
    <string name="descripcion_creditos">Aplicación libre de código abierto distribuida bajo licencia MIT. Desarrollada con fines formativos para aprender Jetpack Compose y consumo de APIs en Android.</string>
    <string name="fuente_datos_creditos">Datos oficiales de tipos de cambio del Banco Central Europeo sincronizados a través de la API abierta de Frankfurter.</string>
    <string name="cerrar">Cerrar</string>

    <string name="divisa_eur">Euro</string>
    <string name="divisa_usd">Dólar estadounidense</string>
    <string name="divisa_gbp">Libra esterlina</string>
    <string name="divisa_try">Lira turca</string>
    <string name="divisa_jpy">Yen japonés</string>
    <string name="divisa_chf">Franco suizo</string>
</resources>
```

> [!TIP]
> Siguiendo esta misma estructura limpia, el proyecto incorpora catálogos para los idiomas con mayor cuota de mercado en la tienda de aplicaciones: francés (`values-fr`), alemán (`values-de`), portugués (`values-pt`), italiano (`values-it`), turco (`values-tr`) y japonés (`values-ja`).

---

## 5. Modelos de datos

Crea el archivo `app/src/main/java/es/gestal/conversordivisas/data/DivisaModelos.kt`. Observa cómo `DivisaInfo` utiliza `@get:StringRes val nombreRes: Int` en lugar de una cadena fija, permitiendo que el nombre de cada moneda se adapte dinámicamente al idioma del usuario:

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

## 6. Catálogo de divisas y buscador reactivo

Crea el archivo `app/src/main/java/es/gestal/conversordivisas/data/DivisasCatalogo.kt`. Asocia cada moneda a su bandera emoji nativa y a su identificador de recurso de cadena. La función de filtrado recibe el `Context` para permitir búsquedas tanto por código ISO como por el nombre traducido al idioma del teléfono:

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

## 7. Persistencia ligera con preservación del orden (*Offline-first*)

Para recordar las divisas favoritas en el orden exacto configurado por el usuario, el importe editado y las últimas tasas descargadas del BCE, creamos `PreferenciasDivisas`.

> [!NOTE]
> `SharedPreferences.getStringSet()` no garantiza el orden de inserción de los elementos. Por eso, serializamos la lista de favoritos (`List<String>`) como una cadena JSON con `Gson`, garantizando que la posición que el usuario elija para cada divisa se mantenga intacta.

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

## 8. Cliente HTTP con Retrofit

Crea el archivo `app/src/main/java/es/gestal/conversordivisas/data/DivisaApi.kt`. La interfaz declara la función suspendida `obtenerTasas` y el objeto `RetrofitClient` centraliza la conexión con tiempos de espera seguros de 15 segundos y registro de peticiones:

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

## 9. Resumen del paso

* Configuramos la internacionalización multi-idioma mediante catálogos `strings.xml`.
* Diseñamos un catálogo desacoplado con banderas emoji, referencias localizadas y búsqueda por idioma activo.
* Implementamos la persistencia en JSON para garantizar el orden personalizado de las favoritas.
* El cliente Retrofit queda listo para consumir tipos de cambio con una sola llamada suspendida.

En el **[Paso 02](02_ViewModel_y_Calculo_Reactivo.md)** conectaremos esta capa de datos con la lógica de negocio mediante el `ViewModel`, implementando el cálculo reactivo a 60 FPS, la reordenación de favoritas y la interacción de cambio de base *Tap-to-Base*.
