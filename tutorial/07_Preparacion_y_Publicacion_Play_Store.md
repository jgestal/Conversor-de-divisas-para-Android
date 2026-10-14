# 🚀 Paso 07: Preparación y publicación en Google Play Store

En este paso final completaremos el ciclo de vida profesional de una aplicación Android: desde la generación del almacén de claves criptográficas (*Keystore*) y la compilación del binario oficial de producción (*Android App Bundle - .aab*), hasta el checklist técnico de cumplimiento de políticas para publicar en **Google Play Console**.

---

## 1. Requisitos previos para publicar en Google Play

Para publicar cualquier aplicación en la tienda oficial de Google se requiere:

1. **Cuenta de desarrollador en Google Play Console:** Registro como particular o empresa en [play.google.com/console](https://play.google.com/console) (pago único de registro de 25 $).
2. **Identificador de paquete único (*applicationId*):** En nuestro proyecto es `es.gestal.conversordivisas`. Una vez subido a la tienda, este identificador no se puede modificar.
3. **Android App Bundle (.aab):** Desde agosto de 2021, Google Play exige el formato `.aab` en lugar de los antiguos archivos `.apk`. Google Play utiliza este paquete para generar APKs optimizados y reducidos para cada arquitectura y densidad de pantalla.
4. **Almacén de claves privado (*Upload Keystore*):** Clave criptográfica con la que firmarás digitalmente todos los binarios que subas a la consola.

---

## 2. Generación del almacén de claves criptográficas (*Keystore*)

Para firmar la aplicación en modo `release` necesitamos generar un par de claves privada/pública en un archivo `.jks` (*Java KeyStore*).

### Opción A: Mediante la interfaz gráfica de Android Studio
1. En el menú superior de Android Studio, haz clic en **Build** ➔ **Generate Signed Bundle / APK...**
2. Selecciona **Android App Bundle** y pulsa **Next**.
3. En el apartado *Key store path*, pulsa en **Create new...**
4. Selecciona una ubicación segura fuera del control de versiones (por ejemplo, en tu carpeta de usuario protegida).
5. Rellena los campos obligatorios:
   * **Password:** Contraseña del almacén.
   * **Alias:** Nombre de la clave (por ejemplo: `conversor_key`).
   * **Password de la clave:** Contraseña para el alias.
   * **Validity (years):** Mínimo 25 años.
   * **Certificate:** Tu nombre (**Juan Gestal**), organización y país (`ES`).
6. Pulsa **OK**.

### Opción B: Mediante la herramienta de línea de comandos `keytool`
Si prefieres generarlo desde la terminal:

```bash
keytool -genkey -v \
  -keystore conversor_release.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias conversor_key
```

> [!WARNING]
> Guarda este archivo `.jks` y sus contraseñas en un lugar seguro con copia de seguridad. Nunca lo incluyas en Git (añádelo al archivo `.gitignore`). Si pierdes el almacén de claves de una app que no use *Play Feature Delivery*, no podrás volver a actualizar la aplicación en la tienda.

---

## 3. Configuración de versiones y compilación del App Bundle (.aab)

### 3.1. Gestión de versiones en app/build.gradle.kts
Cada vez que subas una nueva versión a Google Play debes incrementar el `versionCode` (un número entero correlativo) y opcionalmente el `versionName` (la versión legible para los usuarios):

```kotlin
android {
    defaultConfig {
        applicationId = "es.gestal.conversordivisas"
        minSdk = 26
        targetSdk = 37
        versionCode = 1       // Incrementar en cada actualización (2, 3, 4...)
        versionName = "1.0.0" // Versión visible en la ficha de la tienda
    }

    buildTypes {
        release {
            // En proyectos de producción se activa R8/ProGuard para ofuscación y reducción de tamaño
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### 3.2. Compilación del binario de producción
Ejecuta la tarea de Gradle correspondiente desde la terminal en la raíz de `app/`:

```bash
./gradlew bundleRelease
```

El binario resultante se generará en:
```text
app/build/outputs/bundle/release/app-release.aab
```

---

## 4. Recursos gráficos obligatorios para la ficha de Play Store

Google Play exige un conjunto mínimo de recursos gráficos para validar la ficha de la aplicación. En el directorio `docs/play_store/assets/` dispones de todos los elementos listos para su uso:

| Recurso | Archivo en el proyecto | Formato y dimensiones | Propósito |
| :--- | :--- | :--- | :--- |
| **Icono vectorial fuente** | `docs/play_store/assets/app_icon.svg` | SVG vectorial con geometría pura | Fuente editable para futuras adaptaciones y marketing. |
| **Icono oficial de Play Store** | `docs/play_store/assets/app_icon_512x512.png` | PNG 512 x 512 px (32 bits, fondo opaco) | Icono que se mostrará en los resultados de búsqueda y fichas. |
| **Gráfico de funciones (Feature Graphic)** | `docs/play_store/assets/feature_graphic_1024x500.png` | PNG 1024 x 500 px (banner con capturas reales) | Imagen de cabecera visual en la parte superior de la ficha de Play Store. |
| **Captura 01: Pantalla principal** | `docs/play_store/assets/screenshots/01_pantalla_principal_hero.png` | PNG 1080 x 2400 px | Muestra la tarjeta Hero de EUR activa y la lista de favoritas. |
| **Captura 02: Tap-to-Base** | `docs/play_store/assets/screenshots/02_tap_to_base_lira_turca.png` | PNG 1080 x 2400 px | Demuestra la inversión instantánea de cálculo (Lira turca ⇄ Euro). |
| **Captura 03: Modal de gestión** | `docs/play_store/assets/screenshots/03_modal_bottom_sheet_gestion.png` | PNG 1080 x 2400 px | Ilustra el catálogo deslizable, buscador y selección con un solo toque. |
| **Captura 04: Créditos y autoría** | `docs/play_store/assets/screenshots/04_dialogo_creditos_juan_gestal.png` | PNG 1080 x 2400 px | Exhibe la autoría de Juan Gestal, enlaces web y la licencia MIT. |

> [!TIP]
> **Fichas de Play Store localizadas y ASO:**
> En el archivo `docs/play_store/ficha_play_store.md` dispones de los textos de la ficha (título de 30 caracteres, descripción corta de 80 y descripción completa estructurada con palabras clave) redactados para los 5 idiomas principales de la tienda:
> 1. **Español (ES)**
> 2. **Inglés (EN)**
> 3. **Francés (FR)**
> 4. **Alemán (DE)**
> 5. **Portugués (PT)**
> 
> Puedes copiar y pegar cada sección en Google Play Console en **Ficha de Play Store principal > Gestionar traducciones** para maximizar la visibilidad y descargas internacionales.

---

## 5. Checklist de cumplimiento y políticas de Google Play

Antes de que Google Play apruebe tu aplicación para distribución pública, debes completar varios cuestionarios obligatorios en Play Console (documentados en `docs/play_store/guia_publicacion_checklist.md`):

### 1. Política de privacidad (Privacy Policy)
* **Requisito legal:** Obligatorio para cualquier aplicación con el permiso `android.permission.INTERNET`.
* **Solución:** Tienes la política legal completa redactada en español e inglés en `docs/play_store/politica_de_privacidad.md`. Solo debes alojarla en GitHub Pages o en tu dominio personal (ej. `https://gestal.es/privacidad-conversor`) y enlazar la URL en la consola.

### 2. Seguridad de los datos (Data Safety Form)
* **¿La app recopila o comparte datos del usuario?** ➔ Seleccionar **No**.
* La app únicamente realiza peticiones HTTP anónimas a la API de Frankfurter para obtener los tipos de cambio del BCE, sin enviar identificadores publicitarios, ubicación, cookies ni requerir inicio de sesión.

> [!NOTE]
> **Transparencia en Data Safety:**
> Google somete a escrutinio manual las declaraciones de privacidad. Como nuestro código no incluye librerías de analítica (Firebase Analytics, Google Analytics) ni redes publicitarias (AdMob), la respuesta honesta y verificable en el cuestionario es «No se recopila ningún dato». Esto agiliza enormemente el proceso de revisión.

### 3. Acceso a la aplicación
* Seleccionar **Todas las funciones están disponibles sin restricciones** (no requiere credenciales, suscripciones ni pagos).

### 4. Clasificación de contenido (Cuestionario IARC)
* Rellena el cuestionario oficial de IARC indicando que la aplicación es una herramienta de utilidad / finanzas sin contenido violento ni apuestas.
* Obtendrás automáticamente una clasificación **PEGI 3** (Europa) y **Everyone** (Estados Unidos).

### 5. Audiencia objetivo y contenido infantil
* Selecciona **Mayores de 13 años** o **Todas las edades**.

### 6. Aplicaciones gubernamentales o bancarias
* Declara que la app **no representa a ninguna entidad gubernamental ni es un banco oficial**, sino un cliente educativo de datos públicos del BCE.

---

## 6. Proceso de lanzamiento: pistas de prueba y producción

Google Play Console organiza las publicaciones en diferentes pistas progresivas:

```
┌─────────────────────────┐
│     Pruebas internas    │  ➔ Enlace directo para hasta 100 probadores (revisión inmediata).
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│     Pruebas cerradas    │  ➔ Obligatorio para cuentas personales nuevas (20 probadores durante 14 días).
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│        Producción       │  ➔ Disponible para todo el público en Google Play Store.
└─────────────────────────┘
```

> [!IMPORTANT]
> **Requisito de 14 días de pruebas cerradas (Cuentas personales creadas tras nov. 2023):**
> Si tu cuenta de desarrollador personal de Google Play se creó después del 13 de noviembre de 2023, Google exige que al menos **20 evaluadores** permanezcan registrados en tu pista de pruebas cerradas de forma ininterrumpida durante **14 días naturales**.
> 
> Durante este periodo:
> 1. Sube el `.aab` a la pista de **Pruebas cerradas**.
> 2. Comparte el enlace de adhesión con tus 20 evaluadores.
> 3. Una vez transcurridos los 14 días con los evaluadores activos, aparecerá habilitado el botón para solicitar acceso a Producción.

1. Crea una versión en la pista seleccionada y sube el archivo `app-release.aab`.
2. Añade las notas de la versión en español o inglés (por ejemplo: *«Versión 1.0.0: Conversión reactiva con datos del BCE, soporte multilingüe, reordenación de favoritas y modo sin conexión»*).
3. Revisa los avisos de la consola y pulsa en **Guardar y enviar a revisión**.
4. El equipo de revisión de Google suele tardar entre 24 y 72 horas en validar la aplicación. Una vez aprobada, estará disponible para su descarga en todo el mundo.

---

## 7. Resumen global del proyecto

A lo largo de este tutorial hemos recorrido el ciclo de desarrollo completo de una aplicación real en Android:

1. **Paso 00:** Análisis del problema, diseño de interacción centrado en el viajero (*Tap-to-Base*), reordenación de favoritas y modelado dinámico.
2. **Paso 01:** Internacionalización multilingüe (`strings.xml`), capa de red con Retrofit y persistencia ligera para soporte *offline-first*.
3. **Paso 02:** Gestión de estado unidireccional con `ConversorUiState`, `ViewModel` reactivo a 60 FPS y ordenación personalizada.
4. **Paso 03:** Interfaz moderna en Jetpack Compose con Material 3, hoja deslizante *ModalBottomSheet*, selección rápida de favoritas y créditos de Juan Gestal.
5. **Paso 04:** Animaciones fluidas y microinteracciones en Compose (desaparición de tarjetas al desmarcar favoritas con `animateItem`, física de resortes, transiciones de panel de aeropuerto con `AnimatedContent` e indicadores GPU).
6. **Paso 05:** Generación del almacén de claves criptográficas, App Bundle firmado y preparación para el lanzamiento en Google Play Store.
