# 🛠️ Paso 01: Configuración del proyecto y recursos visuales

En este paso configuraremos el entorno de trabajo en Android Studio con Jetpack Compose, incorporaremos las dependencias necesarias en Gradle, declararemos los permisos en el manifiesto, implementaremos la pantalla de presentación Splash Screen con sus iconos vectoriales nativos y dejaremos lista la infraestructura de localización multi-idioma.

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

> [!TIP]
> **Atención al autocompletado del nombre de paquete:**
> Al escribir «Conversor de divisas» en el campo *Name*, el asistente de Android Studio autocompleta el *Package name* añadiendo la partícula «de» (`es.gestal.conversordedivisas`). Asegúrate de borrar ese «de» para que quede exactamente `es.gestal.conversordivisas`.
> 
> *(Si ya creaste el proyecto con `conversordedivisas`, puedes renombrarlo fácilmente: abre `MainActivity.kt`, edita la primera línea a `package es.gestal.conversordivisas`, pulsa `Alt + Enter` o `Option + Return` sobre el aviso y selecciona **«Move to package 'es.gestal.conversordivisas'»**).*

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

> [!NOTE]
> **Estructura de recursos en proyectos Compose:**
> En los proyectos modernos creados con Jetpack Compose, los temas y paletas de color se gestionan mediante código Kotlin (`ui/theme/Color.kt` y `ui/theme/Theme.kt`). Por este motivo, si inspeccionas el árbol de tu proyecto en Android Studio:
> ```text
> app/
>  ├── manifests/
>  │    └── AndroidManifest.xml
>  ├── kotlin+java/
>  │    └── es.gestal.conversordivisas/
>  │         ├── ui.theme/
>  │         └── MainActivity.kt
>  └── res/
>       ├── mipmap/ (iconos de la app)
>       └── values/
>            └── strings.xml
> ```
> Observarás que **no existen por defecto** ni `colors.xml`, ni `themes.xml`, ni la carpeta `res/drawable/`. 
> 
> No necesitas crear carpetas a mano en el sistema de archivos: el propio asistente gráfico de Android Studio las creará automáticamente siguiendo estos sencillos pasos.

Para evitar avisos de dependencias inexistentes en el entorno de desarrollo, crearemos los archivos en orden: primero el color de fondo, luego el icono vectorial y finalmente el estilo en `themes.xml`:

#### 1. Color de fondo del Splash (`res/values/colors.xml`)

1. En el panel izquierdo de Android Studio (asegúrate de tener seleccionada la vista **Android** en el desplegable superior).
2. Despliega la carpeta **`app`** y después la carpeta **`res`**.
3. Haz clic con el **botón derecho del ratón** sobre la carpeta **`values`** (o sobre **`res`** si `values` aún no está visible).
4. En el menú contextual que aparece, selecciona **New** ➔ **Values Resource File**.
5. En la ventana emergente que se abrirá:
   * **File name:** escribe `colors` (sin `.xml`, el entorno lo añade automáticamente).
   * Deja el resto de campos con sus valores por defecto.
   * Haz clic en **OK**.
6. Android Studio creará el archivo `app/src/main/res/values/colors.xml` y lo abrirá en el editor. Sustituye todo su contenido por:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Color de fondo oscuro de arranque (Dark Slate) -->
    <color name="splash_background">#0F172A</color>
</resources>
```

#### 2. Icono central del Splash Screen (`ic_splash_icon.xml`)

> [!TIP]
> **Generación automática del directorio `drawable`:**
> No es necesario crear la carpeta `drawable` previamente. Al seleccionar el tipo de recurso *Drawable* en el asistente, Android Studio creará automáticamente el directorio `res/drawable/` en el disco.

1. Haz clic con el **botón derecho del ratón** sobre la carpeta **`res`** en el panel de proyecto.
2. Selecciona **New** ➔ **Android Resource File**.
3. En el cuadro de diálogo:
   * **File name:** escribe exactamente `ic_splash_icon`.
   * **Resource type:** abre el menú desplegable y selecciona **`Drawable`**.
   *(Comprobarás que el campo Directory name cambia automáticamente a `drawable`)*.
   * Haz clic en **OK**.
4. Se abrirá el archivo `app/src/main/res/drawable/ic_splash_icon.xml`. Borra cualquier código autogenerado y pega el diseño vectorial completo que combina las flechas de intercambio y las monedas de Euro y Dólar:

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

1. Haz clic con el **botón derecho del ratón** sobre la carpeta **`res`** (o sobre **`res/values`**).
2. Selecciona **New** ➔ **Values Resource File**.
3. En el campo **File name**, escribe: `themes`.
4. Haz clic en **OK**.
5. Ahora que el color (`@color/splash_background`) y el icono (`@drawable/ic_splash_icon`) ya existen en el proyecto, sustituye todo el contenido por la definición del tema de inicio y el tema base:

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

Abre el archivo `app/src/main/res/values/strings.xml` (que ya fue generado por la plantilla de Android Studio) y sustituye su contenido por las cadenas completas en inglés:

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

Para añadir la versión en español, creamos el archivo de cadenas localizado con el asistente de Android Studio:

1. Haz clic derecho sobre la carpeta **`res`**.
2. Selecciona **New** ➔ **Android Resource File**.
3. En **File name**, escribe: `strings`.
4. En **Resource type**, selecciona: **`Values`**.
5. En la lista de calificadores disponibles de la izquierda (**Available qualifiers**), selecciona **`Locale`** y pulsa el botón con dos flechas (**`>>`**) en el centro.
6. En la columna de idiomas (**Language**), selecciona **`es: Spanish`** (en *Specific Region Only* deja *Any Region*).
   *(Observa cómo Directory name cambia a `values-es`)*.
7. Pulsa en **OK**.
8. Android Studio creará automáticamente `app/src/main/res/values-es/strings.xml`. Pega la traducción correspondiente:

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

## 5. Resumen del paso

* Creamos el proyecto base en Android Studio con Jetpack Compose y Kotlin DSL.
* Configuramos las dependencias modernas de arquitectura, Material 3, Splash Screen, corrutinas y Retrofit en `app/build.gradle.kts`.
* Declaramos el permiso de internet obligatorio en `AndroidManifest.xml`.
* Diseñamos e implementamos los recursos visuales del Splash Screen (`colors.xml`, `ic_splash_icon.xml`, `themes.xml`) y los vinculamos al manifiesto.
* Establecimos el sistema de internacionalización i18n con soporte para inglés por defecto y localización al español.

En el **[Paso 02](02_Capa_de_Datos_y_Red.md)** implementaremos la capa de datos completa: los modelos de información con soporte dinámico de recursos, el catálogo de divisas con banderas emoji y motor de búsqueda, la persistencia local en JSON con SharedPreferences y el cliente HTTP Retrofit con corrutinas.
