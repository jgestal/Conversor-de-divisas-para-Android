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

Google Play exige un conjunto mínimo de recursos gráficos para validar la ficha de la aplicación. En el directorio `docs/play_store/assets/` y `docs/play_store/screenshots/` dispones de todos los elementos listos para su uso:

### 4.1. Inventario de recursos maestros

| Recurso | Archivo en el proyecto | Formato y dimensiones | Propósito |
| :--- | :--- | :--- | :--- |
| **Icono vectorial fuente** | `docs/play_store/assets/app_icon.svg` | SVG vectorial con geometría pura | Fuente editable para futuras adaptaciones y marketing. |
| **Icono oficial de Play Store** | `docs/play_store/assets/app_icon_512x512.png` | PNG 512 x 512 px (32 bits, fondo azul `#06294A`) | Icono que se mostrará en los resultados de búsqueda y fichas. |
| **Gráfico de funciones (Feature Graphic)** | `docs/play_store/assets/feature_graphic_1024x500.png` | PNG 1024 x 500 px (banner corporativo con maquetas reales) | Imagen de cabecera visual en la parte superior de la ficha de Play Store. |
| **Captura 01: Pantalla principal** | `docs/play_store/screenshots/01_pantalla_principal_hero.png` | PNG 1080 x 2424 px | Muestra la tarjeta Hero de EUR activa y la lista de favoritas. |
| **Captura 02: Tap-to-Base** | `docs/play_store/screenshots/02_tap_to_base_lira_turca.png` | PNG 1080 x 2424 px | Demuestra la inversión instantánea de cálculo (Lira turca ⇄ Euro). |
| **Captura 03: Modal de gestión** | `docs/play_store/screenshots/03_modal_bottom_sheet_gestion.png` | PNG 1080 x 2424 px | Ilustra el catálogo deslizable, buscador y selección con un solo toque. |
| **Captura 04: Créditos y autoría** | `docs/play_store/screenshots/04_dialogo_creditos_juan_gestal.png` | PNG 1080 x 2424 px | Exhibe la autoría de Juan Gestal, enlaces web y la licencia MIT. |

### 4.2. Estrategia de idiomas para las capturas: ¿Español, inglés o cada idioma?

Una duda habitual al publicar en Google Play es si se deben generar capturas en cada idioma:

1. **Ficha principal predeterminada (Default Store Listing):**
   * Si tu idioma predeterminado es el **Español**, debes subir las capturas en español ubicadas en `docs/play_store/screenshots/`.
   * Cualquier usuario del mundo cuyo idioma nativo no tenga capturas específicas verá estas capturas por defecto.
2. **Ficha internacional en Inglés (Recomendado para conversión global):**
   * En la carpeta `docs/play_store/screenshots/en/` tienes disponible el set idéntico capturado en **Inglés** (`Currency Converter`, `Active currency`, `Favorites`, `Developed by Juan Gestal`, etc.).
   * Al añadir la traducción al inglés en Google Play Console, sube estas capturas para que todos los usuarios angloparlantes y de mercados globales vean la interfaz en su idioma.
3. **¿Hacer capturas para francés, alemán, portugués, etc.?**
   * No es estrictamente necesario salvo que tengas un volumen de descargas muy alto en esos países. Google Play reutiliza automáticamente las capturas de la ficha predeterminada si un idioma localizado no incluye capturas propias. La combinación **Español (base) + Inglés (internacional)** es el estándar profesional más eficiente.

### 4.3. Dónde subir los recursos gráficos en Google Play Console

* **URL directa:** [play.google.com/console](https://play.google.com/console/) ➔ Selecciona tu aplicación (`Conversor de divisas`).
* **Ruta en el menú lateral:**
  ```text
  Crecimiento
  └── Presencia en Google Play Store
      ├── Ficha de Play Store principal    <-- Sube aquí app_icon_512x512.png, feature_graphic_1024x500.png y capturas en español
      └── (Botón superior) "Gestionar traducciones" --> Añadir "Inglés (Estados Unidos)" y subir capturas de docs/play_store/screenshots/en/
  ```
* **Estructura de URL directa en consola:**
  `https://play.google.com/console/u/0/developers/<TU_ID_DESARROLLADOR>/app/<TU_ID_APP>/key-store-listing`

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

Google Play Console organiza las publicaciones en diferentes pistas progresivas con URLs directas dedicadas:

```
┌─────────────────────────┐
│     Pruebas internas    │  ➔ Hasta 100 evaluadores con aprobación inmediata (minutos).
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│     Pruebas cerradas    │  ➔ Obligatorio para cuentas personales nuevas: 20 probadores durante 14 días.
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│        Producción       │  ➔ Publicación global en Google Play Store para todos los usuarios.
└─────────────────────────┘
```

### 6.1. Direcciones y secciones en Google Play Console

Para gestionar cada fase de lanzamiento, accede a [play.google.com/console](https://play.google.com/console/) y utiliza estas rutas en el menú lateral de tu aplicación:

| Sección | Ruta en el menú lateral | Enlace / Estructura de URL directa |
| :--- | :--- | :--- |
| **Panel de la app** | `Panel de control` | `https://play.google.com/console/u/0/developers/<ID_DEV>/app/<ID_APP>/app-dashboard` |
| **Pruebas internas** | `Pruebas` ➔ `Pruebas internas` | `https://play.google.com/console/u/0/developers/<ID_DEV>/app/<ID_APP>/internal-testing` |
| **Pruebas cerradas** | `Pruebas` ➔ `Pruebas cerradas` | `https://play.google.com/console/u/0/developers/<ID_DEV>/app/<ID_APP>/tracks/closed-testing` |
| **Producción** | `Lanzamiento` ➔ `Producción` | `https://play.google.com/console/u/0/developers/<ID_DEV>/app/<ID_APP>/production` |
| **Contenido legal** | `Directrices` ➔ `Contenido de la aplicación` | `https://play.google.com/console/u/0/developers/<ID_DEV>/app/<ID_APP>/app-content` |

---

### 6.2. Guía paso a paso: Cómo conseguir 20 evaluadores durante 14 días naturales

> [!IMPORTANT]
> **Normativa de Google para cuentas personales (desde 13 nov. 2023):**
> Si tu cuenta de desarrollador de Google Play es de tipo personal y fue dada de alta después del 13 de noviembre de 2023, Google no te permitirá publicar directamente en Producción. Exige que al menos **20 evaluadores permanezcan registrados (*opt-in*) en una prueba cerrada durante 14 días naturales ininterrumpidos**. (Las cuentas de empresa u organizaciones con D-U-N-S están exentas).

A continuación tienes la metodología estándar de la industria para superar este requisito con éxito:

#### Paso 1: Configurar la lista de evaluadores con Grupos de Google (*Google Groups*)
En lugar de introducir correos de Gmail uno por uno, la vía más ágil y profesional es usar un Grupo de Google:
1. Entra en [groups.google.com](https://groups.google.com/) y crea un grupo público (por ejemplo: `conversor-divisas-testers@googlegroups.com`).
2. Configura los permisos para que cualquiera con el enlace pueda unirse al grupo.
3. En Play Console, ve a **Pruebas** ➔ **Pruebas cerradas** ➔ selecciona tu pista (o crea una llamada *Alfa*) ➔ pestaña **Evaluadores**.
4. En el método de gestión, marca **Grupos de Google** y escribe la dirección de correo del grupo recién creado.
5. Al final de la página encontrarás los dos enlaces oficiales de adhesión (*opt-in links*):
   * **Enlace web:** `https://play.google.com/apps/testing/es.gestal.conversordivisas`
   * **Enlace Android (móvil):** Abre la ficha de prueba directamente en la app de Play Store.
   * *Nota:* Para que los evaluadores puedan ver la app, primero deben unirse al Grupo de Google y luego abrir el enlace de adhesión para pulsar en **"Aceptar invitación / Convertirme en tester"**.

#### Paso 2: ¿Dónde y cómo conseguir los evaluadores? (Estrategias efectivas)
1. **Comunidades de testeo mutuo entre desarrolladores (*Peer-to-Peer*):**
   * **Reddit (`r/AndroidClosedTesting`):** Es la mayor comunidad internacional (más de 30.000 desarrolladores independientes). La dinámica habitual es de apoyo mutuo: publicas un post con el link de tu Grupo de Google y tus enlaces de opt-in ofreciendo probar la app de quien pruebe la tuya (*«Test for test - 14 days»*).
   * **Plataforma Testers Community:** Existe una aplicación comunitaria gratuita en Android y web ([testers-community.com](https://testers-community.com/)) creada específicamente para que desarrolladores indie intercambien pruebas y mantengan las apps instaladas los 14 días requeridos con métricas automáticas.
   * **Comunidades de Discord y Telegram:** Grupos de Kotlin y Android Developers en español (como comunidades de GDG - Google Developer Groups, Betabeers o canales de desarrolladores móviles).
2. **Círculo personal y profesional:**
   * Amigos, familiares, compañeros de clase de la universidad o bootcamp, y colegas de trabajo que dispongan de un teléfono Android personal.
3. **Regla de oro del margen de seguridad (Recluta entre 25 y 30 testers):**
   * El contador de 14 días de Google se detiene si en algún momento el número de evaluadores activos cae por debajo de 20 (por ejemplo, si alguien se da de baja del grupo o abandona el programa).
   * **Consejo estratégico:** Consigue siempre entre **25 y 30 evaluadores**. De este modo, si 2 o 3 usuarios desinstalan la app o se desapuntan, tu cuenta se mantendrá por encima del umbral mínimo y no perderás el tiempo acumulado.

##### Plantilla lista para publicar en `r/AndroidClosedTesting`:
```text
Title: [Closed Testing] Currency Converter (Instant rates & offline) - Need 20 testers (Will test back 14 days!)

Body:
Hi everyone!
I'm looking for 20 testers for my open-source Currency Converter app built with Jetpack Compose & Material 3.
I will happily test your application back every day during the 14 days!

1. Join the Google Group first:
   https://groups.google.com/g/tu-grupo-testers

2. Join the test on the Web:
   https://play.google.com/apps/testing/es.gestal.conversordivisas

3. Download on Google Play Store:
   https://play.google.com/store/apps/details?id=es.gestal.conversordivisas

Please leave a comment below with your Google Group and app links, and I will install and review yours right away!
```

> [!WARNING]
> **Resolución del error frecuente: «Elemento no encontrado» o «Error 404 en Play Store»:**
> Casi el 90% de las incidencias reportadas por los evaluadores al intentar descargar la app se deben a una de estas tres causas:
> 1. **No haberse unido primero al Grupo de Google:** Si el usuario abre el enlace de la app sin pertenecer al grupo, Google Play deniega el acceso y devuelve un error 404.
> 2. **Conflicto de cuentas de Google:** En teléfonos con varias cuentas de Gmail sincronizadas, el navegador web puede estar autenticado con una cuenta y la app de Play Store con otra. El evaluador debe asegurarse de que la cuenta activa en Play Store sea la misma que se unió al grupo.
> 3. **La versión en pruebas aún está en revisión por Google:** La pista cerrada debe haber sido aprobada por el equipo de Google (estado *«Publicado»* o *«Activo»* en Play Console). Si aún figura en estado *«En revisión»*, nadie fuera de las pruebas internas podrá descargarla.

#### Paso 3: Interacción, telemetría y actualizaciones durante los 14 días
Google no solo monitoriza el número de registros en la consola; sus algoritmos analizan señales de uso real para verificar que no son cuentas bot o falsas:
* Pide a tus evaluadores que **abran la aplicación 2 o 3 veces por semana**.
* Anímalos a realizar conversiones reales, cambiar la moneda base (*Tap-to-Base*), marcar/desmarcar favoritas y abrir la pantalla de créditos.
* Pídeles que dejen una breve reseña o comentario privado en la ficha de prueba de Google Play. El feedback cualitativo es una de las métricas que Google evalúa positivamente.

> [!TIP]
> **¿Publicar una actualización reinicia el contador de 14 días?**
> **Rotundamente NO.** Puedes subir nuevas versiones (`1.0.1`, `1.0.2`...) a la pista de pruebas cerradas para corregir fallos o incorporar sugerencias de los evaluadores. Subir un nuevo `.aab` **no reinicia el temporizador de 14 días** siempre que la pista de pruebas permanezca activa y mantenga los 20+ evaluadores. Al contrario, demuestra a los revisores de Google que estás iterando activamente sobre el producto antes de lanzarlo.

#### Paso 4: El cuestionario de solicitud de acceso a Producción
Cuando el contador llegue a 14 días ininterrumpidos con 20+ evaluadores, en el **Panel de control** de Play Console se habilitará el botón **Solicitar acceso a producción** (*Apply for production*).

Google te presentará un formulario exhaustivo de tres secciones que un revisor humano evaluará. Prepárate para responder con rigor y enfoque profesional:

##### Sección A: Sobre tu proceso de pruebas cerradas
1. **¿Cómo reclutaste a tus evaluadores?**
   * *Ejemplo de respuesta recomendada:* «Recluté evaluadores mediante un Grupo de Google público distribuido entre compañeros de desarrollo de software, comunidades técnicas de Android (`r/AndroidClosedTesting`) y círculos de estudio de Kotlin».
2. **¿Qué facilidad tuvieron los evaluadores para probar la app?**
   * *Ejemplo:* «Se les proporcionó una guía clara paso a paso para unirse al Grupo de Google y acceder mediante el enlace oficial de opt-in de Google Play Store sin incidencias de permisos».
3. **¿Qué comentarios o sugerencias recibiste de los evaluadores?**
   * *Ejemplo:* «Los evaluadores elogiaron la rapidez del cálculo aritmético reactivo y la utilidad del modo sin conexión. Como sugerencias de mejora, pidieron controles más evidentes para reordenar las monedas favoritas en la lista principal y consultar la autoría y licencia del proyecto».
4. **¿Qué cambios o correcciones realizaste basándote en ese feedback?**
   * *Ejemplo:* «Añadimos flechas de reordenación directa en las tarjetas de favoritas, refinamos el contraste de la paleta corporativa en modo claro y oscuro, y creamos la ventana modal de créditos con la foto del autor y enlaces a su sitio web».

##### Sección B: Sobre tu aplicación y público objetivo
5. **¿A quién va dirigida la aplicación y qué necesidad resuelve?**
   * *Ejemplo:* «Está orientada a viajeros internacionales y profesionales que necesitan consultar tipos de cambio de divisas al instante, con especial resiliencia en zonas sin cobertura o itinerancia (modo offline) mediante tipos oficiales del Banco Central Europeo».

##### Sección C: Preparación para producción
6. **¿Por qué consideras que tu aplicación está lista para el lanzamiento público?**
   * *Ejemplo:* «Durante los 14 días de prueba la aplicación mantuvo una tasa de estabilidad del 100% en Android Vitals, con 0 bloqueos (ANRs) y 0 fallos críticos (crashes). Cumple íntegramente con todas las políticas de privacidad, seguridad de datos (sin recopilación de información personal) y diseño accesible de Google Play».

---

### 6.3. Subida del binario final y notas de versión

1. En la pista de **Pruebas cerradas** (o **Producción** una vez autorizada), pulsa en **Crear nueva versión**.
2. Arrastra el archivo firmado:
   ```text
   app/build/outputs/bundle/release/app-release.aab
   ```
3. En el campo **Notas de la versión** (*Release Notes*), incluye un resumen bilingüe:
   ```text
   es-ES:
   Versión 1.0.0: Lanzamiento del Conversor de divisas.
   • Cálculo reactivo e instantáneo con tipos de cambio oficiales del BCE.
   • Modo sin conexión (offline-first) y soporte multilingüe en 8 idiomas.
   • Interacción Tap-to-Base y reordenación de favoritas personalizada.
   • Interfaz Jetpack Compose moderna, libre de publicidad y con licencia MIT.

   en-US:
   Version 1.0.0: Initial release of Currency Converter.
   • Instant reactive calculations powered by official ECB exchange rates.
   • 100% offline-first resilience and 8-language localization.
   • Innovative Tap-to-Base interaction with customizable favorite order.
   • Clean Jetpack Compose UI, ad-free and open-source under MIT license.
   ```
4. Haz clic en **Guardar** ➔ **Revisar versión** ➔ **Iniciar el lanzamiento**.
5. El equipo de revisión de Google suele validar la versión en un plazo de **24 a 72 horas**. ¡Tu aplicación estará activa en Google Play!

---

## 7. Resumen global del proyecto

A lo largo de este tutorial hemos recorrido el ciclo de desarrollo completo de una aplicación real en Android:

1. **[Paso 00: Requisitos, diseño de interacción y análisis de la API pública](00_Especificaciones_Diseno_y_Analisis_API.md):** Modelado dinámico del problema, caso de uso del viajero (*Tap-to-Base*) y análisis del endpoint del BCE sin API keys.
2. **[Paso 01: Configuración del proyecto y recursos visuales](01_Configuracion_del_Proyecto_y_Recursos.md):** Gradle Kotlin DSL, Splash Screen API oficial con diseño corporativo en azul marino, iconos adaptativos y catálogos multi-idioma (i18n).
3. **[Paso 02: Arquitectura y estructura del proyecto](02_Arquitectura_y_Estructura_del_Proyecto.md):** Patrón MVVM, flujo unidireccional de datos (*UDF*) y separación limpia de capas (Data, Domain, Presentation).
4. **[Paso 03: Capa de datos y red](03_Capa_de_Datos_y_Red.md):** Modelos DTO con Gson, catálogo de divisas con banderas emoji nativas, persistencia ligera en `SharedPreferences` (*Offline-first*) y cliente `Retrofit` con corrutinas de Kotlin.
5. **[Paso 04: ViewModel y cálculo reactivo en tiempo real](04_ViewModel_y_Calculo_Reactivo.md):** Lógica matemática en memoria a 60 FPS, estado único e inmutable mediante `ConversorUiState`, `StateFlow` y reordenación reactiva de favoritas.
6. **[Paso 05: Interfaz gráfica con Jetpack Compose y Material 3](05_Interfaz_Grafica_Jetpack_Compose.md):** Sistema de temas con paleta corporativa, avatar WebP de autoría de Juan Gestal, componentes modulares (`TarjetaHeroActiva`, `TarjetaDivisaFavorita`), hoja deslizable `ModalBottomSheet`, diálogo de créditos y pantalla amigable de estado vacío.
7. **[Paso 06: Animaciones y microinteracciones en Compose](06_Animaciones_y_Microinteracciones_en_Compose.md):** Desaparición fluida de tarjetas con `Modifier.animateItem()`, física de resortes elásticos, transiciones estilo panel con `AnimatedContent` y aceleración por GPU.
8. **[Paso 07: Preparación y publicación en Google Play Store](07_Preparacion_y_Publicacion_Play_Store.md):** Almacén de claves criptográficas (*Keystore*), compilación del *Android App Bundle* (`.aab`), recursos gráficos bilingües, estrategia de 20 evaluadores durante 14 días y despliegue oficial.
