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
    <!-- Color de fondo azul marino corporativo de arranque -->
    <color name="splash_background">#06294A</color>
</resources>
```

#### 2. Icono central del Splash Screen (`ic_splash_icon.xml`)

> [!TIP]
> **Generación automática del directorio `drawable` y ubicación de recursos:**
> * Los recursos fuente vectoriales maestros se encuentran organizados en la carpeta `docs/assets/` (`docs/assets/icon-1024.svg` y `docs/assets/splash_icon.svg`).
> * La fotografía de perfil del autor está disponible en `docs/assets/profile_picture.png` (original de 1024x1024) y `docs/assets/foto_juan_gestal.png`.
> * La SplashScreen API de Android 12+ requiere un lienzo de **288 × 288 dp** con una **zona segura circular de 192 dp** en el centro.

1. Haz clic con el **botón derecho del ratón** sobre la carpeta **`res`** en el panel de proyecto.
2. Selecciona **New** ➔ **Android Resource File**.
3. En el cuadro de diálogo:
   * **File name:** escribe exactamente `ic_splash_icon`.
   * **Resource type:** abre el menú desplegable y selecciona **`Drawable`**.
   *(Comprobarás que el campo Directory name cambia automáticamente a `drawable`)*.
   * Haz clic en **OK**.
4. Se abrirá el archivo `app/src/main/res/drawable/ic_splash_icon.xml`. Borra cualquier código autogenerado y pega el diseño vectorial oficial centrado con la geometría corporativa en azul marino y las monedas de Euro y Dólar:

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="288dp"
    android:height="288dp"
    android:viewportWidth="1024"
    android:viewportHeight="1024">

    <!-- Recorte circular oficial de la SplashScreen API (diámetro 192dp en lienzo de 288dp) -->
    <group>
        <clip-path android:pathData="M 512,170.7 A 341.3,341.3 0 1,0 512,853.3 A 341.3,341.3 0 1,0 512,170.7 Z" />

        <!-- Fondo azul marino base -->
        <path
            android:fillColor="#06294A"
            android:pathData="M 0,0 L 1024,0 L 1024,1024 L 0,1024 Z" />

        <!-- Diagonal gris claro girada 45 grados -->
        <group
            android:pivotX="858.78"
            android:pivotY="1036.22"
            android:rotation="45">
            <path
                android:fillColor="#D8D8D8"
                android:strokeColor="#979797"
                android:strokeWidth="1"
                android:pathData="M 243.78,121.22 h 1230 v 1830 h -1230 Z" />
        </group>

        <!-- Peldaños de la geometría diagonal -->
        <path
            android:fillColor="#D8D8D8"
            android:pathData="M 388.24,582.04 h 54.40 v 54.40 h -54.40 Z" />
        <path
            android:fillColor="#06294A"
            android:pathData="M 581.36,388.24 h 54.40 v 54.40 h -54.40 Z" />

        <!-- Monedas escaladas y centradas en la zona segura -->
        <group
            android:pivotX="512"
            android:pivotY="512"
            android:scaleX="0.68"
            android:scaleY="0.68">

            <!-- Moneda Euro (Oro) -->
            <path
                android:fillColor="#E0B01B"
                android:strokeColor="#979797"
                android:strokeWidth="1"
                android:pathData="M 305,140.5 A 217.5,217.5 0 1,0 305,575.5 A 217.5,217.5 0 1,0 305,140.5 Z" />
            <path
                android:fillColor="#E0B01B"
                android:pathData="M 305,162 A 196,196 0 1,0 305,554 A 196,196 0 1,0 305,162 Z" />
            <path
                android:fillColor="#00000000"
                android:strokeColor="#06294A"
                android:strokeWidth="4"
                android:pathData="M 305,164 A 194,194 0 1,0 305,552 A 194,194 0 1,0 305,164 Z" />
            <path
                android:fillColor="#06294A"
                android:pathData="M246.8359375 374.34375Q246.3671875 368.484375 246.3671875 362.0390625Q246.3671875 356.4140625 246.71875 351.0234375H212.96875L217.890625 327.46875H250.0Q254.921875 306.4921875 266.7578125 292.3125Q284.453125 270.984375 311.875 270.984375Q348.4375 270.984375 365.4296875 299.578125Q374.8046875 315.6328125 375.5078125 331.8046875H345.15625Q342.2265625 319.3828125 337.5390625 313.0546875Q329.3359375 301.8046875 313.046875 301.8046875Q296.5234375 301.8046875 286.9140625 317.7421875Q284.3359375 322.078125 282.4609375 327.46875H328.984375L324.0625 351.0234375H277.7734375Q277.421875 356.53125 277.421875 362.625Q277.421875 368.8359375 277.7734375 374.34375H321.1328125L316.2109375 397.8984375H283.046875Q284.921875 402.46875 287.5 406.21875Q297.578125 420.6328125 313.1640625 420.6328125Q329.1015625 420.6328125 337.421875 408.2109375Q342.109375 401.53125 345.15625 388.171875H375.15625Q371.171875 416.4140625 355.0 434.109375Q338.828125 451.8046875 313.515625 451.8046875Q282.2265625 451.8046875 264.296875 427.8984375Q254.921875 415.359375 250.46875 397.8984375H212.96875L217.890625 374.34375Z" />

            <!-- Moneda Dólar (Plata) -->
            <path
                android:fillColor="#C4C0B4"
                android:strokeColor="#979797"
                android:strokeWidth="1"
                android:pathData="M 719,447.5 A 217.5,217.5 0 1,0 719,882.5 A 217.5,217.5 0 1,0 719,447.5 Z" />
            <path
                android:fillColor="#C4C0B4"
                android:strokeColor="#06294A"
                android:strokeWidth="4"
                android:pathData="M 719,471 A 194,194 0 1,0 719,859 A 194,194 0 1,0 719,471 Z" />
            <path
                android:fillColor="#06294A"
                android:pathData="M703.5625 664.5859375Q681.1796875 658.0234375 671.98046875 646.3046875Q662.78125 634.5859375 662.78125 618.0625Q662.78125 607.1640625 666.35546875 598.2578125Q669.9296875 589.3515625 676.375 582.90625Q684.6953125 574.5859375 694.890625 571.421875Q701.1015625 569.4296875 713.0546875 568.4921875V552.90625H724.1875V568.7265625Q743.2890625 570.25 754.1875 578.1015625Q773.9921875 590.640625 774.6953125 618.765625H743.875Q742.9375 608.3359375 740.359375 603.53125Q735.90625 595.2109375 724.1875 594.390625V639.0390625Q752.078125 648.6484375 761.6875 656.03125Q777.5078125 668.3359375 777.5078125 690.71875Q777.5078125 720.25 755.828125 733.609375Q742.5859375 741.8125 724.421875 743.21875V766.7734375H713.0546875V743.453125Q689.96875 740.875 678.6015625 733.375Q658.5625 719.8984375 658.9140625 687.4375H690.5546875Q692.1953125 702.203125 695.125 707.2421875Q699.6953125 715.09375 713.0546875 717.4375V667.3984375ZM724.1875 671.265625V717.0859375Q733.4453125 715.9140625 737.6640625 712.8671875Q745.046875 707.4765625 745.046875 694.234375Q745.046875 684.15625 738.1328125 678.296875Q734.03125 674.8984375 724.1875 671.265625ZM713.0546875 635.7578125V594.625Q702.859375 594.859375 697.9375 600.07421875Q693.015625 605.2890625 693.015625 614.3125Q693.015625 624.15625 700.3984375 629.8984375Q704.5 633.0625 713.0546875 635.7578125Z" />
        </group>
    </group>

    <!-- Anillo perimetral del emblema para delimitarlo con elegancia sobre el fondo azul -->
    <path
        android:fillColor="#00000000"
        android:strokeColor="#D8D8D8"
        android:strokeAlpha="0.3"
        android:strokeWidth="6"
        android:pathData="M 512,170.7 A 341.3,341.3 0 1,0 512,853.3 A 341.3,341.3 0 1,0 512,170.7 Z" />
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

### 3.3. Icono adaptativo de la aplicación (Adaptive Icon) y recursos en `docs/assets/`

Los archivos originales y recursos fuente maestros del proyecto se organizan en la carpeta **`docs/assets/`**:
* `docs/assets/icon-1024.svg` / `docs/assets/app_icon.svg`: Vectorial maestro (1024 × 1024 px).
* `docs/assets/splash_icon.svg`: Vectorial optimizado para la Splash Screen oficial (288 × 288 dp con máscara circular de 192 dp).
* `docs/assets/profile_picture.png`: Fotografía pixel-art original de **Juan Gestal** (1024 × 1024 px).
* `docs/assets/foto_juan_gestal.png`: Fotografía optimizada para documentación y visualizaciones.

Para el icono de la aplicación en el escritorio de Android, implementamos un **icono adaptativo (*Adaptive Icon*)** compuesto por dos capas vectoriales en `res/drawable/`:

#### 1. Fondo adaptativo (`app/src/main/res/drawable/ic_launcher_background.xml`):
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="1024"
    android:viewportHeight="1024">

    <!-- Fondo base azul marino corporativo del icono -->
    <path
        android:fillColor="#06294A"
        android:pathData="M 0,0 L 1024,0 L 1024,1024 L 0,1024 Z" />

    <!-- Mitad diagonal gris claro girada 45 grados -->
    <group
        android:pivotX="858.78"
        android:pivotY="1036.22"
        android:rotation="45">
        <path
            android:fillColor="#D8D8D8"
            android:strokeColor="#979797"
            android:strokeWidth="1"
            android:pathData="M 243.78,121.22 h 1230 v 1830 h -1230 Z" />
    </group>

    <!-- Peldaños de transición geométrica -->
    <path
        android:fillColor="#D8D8D8"
        android:pathData="M 388.24,582.04 h 54.40 v 54.40 h -54.40 Z" />
    <path
        android:fillColor="#06294A"
        android:pathData="M 581.36,388.24 h 54.40 v 54.40 h -54.40 Z" />
</vector>
```

#### 2. Primer plano adaptativo (`app/src/main/res/drawable/ic_launcher_foreground.xml`):
Las monedas se escalan al 68% y se centran en el punto `(512, 512)` para encajar estrictamente dentro de la **zona segura circular de 72 dp** (evitando cualquier recorte en lanzadores circulares o *squircles*):

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="1024"
    android:viewportHeight="1024">

    <!-- Monedas Euro y Dólar escaladas y centradas en la zona segura circular (72dp) -->
    <group
        android:pivotX="512"
        android:pivotY="512"
        android:scaleX="0.68"
        android:scaleY="0.68">

        <!-- Moneda Euro (Oro) -->
        <path
            android:fillColor="#E0B01B"
            android:strokeColor="#979797"
            android:strokeWidth="1"
            android:pathData="M 305,140.5 A 217.5,217.5 0 1,0 305,575.5 A 217.5,217.5 0 1,0 305,140.5 Z" />
        <path
            android:fillColor="#E0B01B"
            android:pathData="M 305,162 A 196,196 0 1,0 305,554 A 196,196 0 1,0 305,162 Z" />
        <path
            android:fillColor="#00000000"
            android:strokeColor="#06294A"
            android:strokeWidth="4"
            android:pathData="M 305,164 A 194,194 0 1,0 305,552 A 194,194 0 1,0 305,164 Z" />
        <path
            android:fillColor="#06294A"
            android:pathData="M246.8359375 374.34375Q246.3671875 368.484375 246.3671875 362.0390625Q246.3671875 356.4140625 246.71875 351.0234375H212.96875L217.890625 327.46875H250.0Q254.921875 306.4921875 266.7578125 292.3125Q284.453125 270.984375 311.875 270.984375Q348.4375 270.984375 365.4296875 299.578125Q374.8046875 315.6328125 375.5078125 331.8046875H345.15625Q342.2265625 319.3828125 337.5390625 313.0546875Q329.3359375 301.8046875 313.046875 301.8046875Q296.5234375 301.8046875 286.9140625 317.7421875Q284.3359375 322.078125 282.4609375 327.46875H328.984375L324.0625 351.0234375H277.7734375Q277.421875 356.53125 277.421875 362.625Q277.421875 368.8359375 277.7734375 374.34375H321.1328125L316.2109375 397.8984375H283.046875Q284.921875 402.46875 287.5 406.21875Q297.578125 420.6328125 313.1640625 420.6328125Q329.1015625 420.6328125 337.421875 408.2109375Q342.109375 401.53125 345.15625 388.171875H375.15625Q371.171875 416.4140625 355.0 434.109375Q338.828125 451.8046875 313.515625 451.8046875Q282.2265625 451.8046875 264.296875 427.8984375Q254.921875 415.359375 250.46875 397.8984375H212.96875L217.890625 374.34375Z" />

        <!-- Moneda Dólar (Plata) -->
        <path
            android:fillColor="#C4C0B4"
            android:strokeColor="#979797"
            android:strokeWidth="1"
            android:pathData="M 719,447.5 A 217.5,217.5 0 1,0 719,882.5 A 217.5,217.5 0 1,0 719,447.5 Z" />
        <path
            android:fillColor="#C4C0B4"
            android:strokeColor="#06294A"
            android:strokeWidth="4"
            android:pathData="M 719,471 A 194,194 0 1,0 719,859 A 194,194 0 1,0 719,471 Z" />
        <path
            android:fillColor="#06294A"
            android:pathData="M703.5625 664.5859375Q681.1796875 658.0234375 671.98046875 646.3046875Q662.78125 634.5859375 662.78125 618.0625Q662.78125 607.1640625 666.35546875 598.2578125Q669.9296875 589.3515625 676.375 582.90625Q684.6953125 574.5859375 694.890625 571.421875Q701.1015625 569.4296875 713.0546875 568.4921875V552.90625H724.1875V568.7265625Q743.2890625 570.25 754.1875 578.1015625Q773.9921875 590.640625 774.6953125 618.765625H743.875Q742.9375 608.3359375 740.359375 603.53125Q735.90625 595.2109375 724.1875 594.390625V639.0390625Q752.078125 648.6484375 761.6875 656.03125Q777.5078125 668.3359375 777.5078125 690.71875Q777.5078125 720.25 755.828125 733.609375Q742.5859375 741.8125 724.421875 743.21875V766.7734375H713.0546875V743.453125Q689.96875 740.875 678.6015625 733.375Q658.5625 719.8984375 658.9140625 687.4375H690.5546875Q692.1953125 702.203125 695.125 707.2421875Q699.6953125 715.09375 713.0546875 717.4375V667.3984375ZM724.1875 671.265625V717.0859375Q733.4453125 715.9140625 737.6640625 712.8671875Q745.046875 707.4765625 745.046875 694.234375Q745.046875 684.15625 738.1328125 678.296875Q734.03125 674.8984375 724.1875 671.265625ZM713.0546875 635.7578125V594.625Q702.859375 594.859375 697.9375 600.07421875Q693.015625 605.2890625 693.015625 614.3125Q693.015625 624.15625 700.3984375 629.8984375Q704.5 633.0625 713.0546875 635.7578125Z" />
    </group>
</vector>
```

#### 3. Declaración adaptativa en `res/mipmap-anydpi/ic_launcher.xml` e `ic_launcher_round.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

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

En el **[Paso 02](02_Arquitectura_y_Estructura_del_Proyecto.md)** analizaremos la arquitectura de la aplicación (MVVM + UDF) y el mapa completo de paquetes y responsabilidades antes de escribir la capa de datos.
