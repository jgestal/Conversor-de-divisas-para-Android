# 🚀 Guía y checklist completo para publicar en Google Play Console

Este documento detalla paso a paso todos los requisitos técnicos, legales y visuales necesarios para subir y publicar la aplicación **Conversor de divisas** (`es.gestal.conversordivisas`) en **Google Play Console**.

---

## 1. Requisitos de cuenta y verificación de desarrollador

1. **Cuenta en Google Play Console:**
   - Registro en [play.google.com/console](https://play.google.com/console) con un pago único de 25 $.
2. **Verificación de identidad oficial (Normativa de Google):**
   - **Cuentas particulares:** Verificación de documento de identidad (DNI/Pasaporte) y comprobante de domicilio.
   - **Cuentas de organización / empresa:** Número D-U-N-S oficial y registro mercantil.
3. **Requisito de pruebas para cuentas personales (nuevas desde nov. 2023):**
   - Si tu cuenta de desarrollador es personal y fue creada a partir de noviembre de 2023, Google exige realizar una prueba cerrada con un mínimo de **20 evaluadores (*testers*) durante al menos 14 días consecutivos** antes de solicitar el acceso a producción. (Las cuentas de empresa u organizaciones están exentas de este requisito).

---

## 2. Inventario de recursos gráficos requeridos

Todos los recursos gráficos ya han sido generados y organizados en la carpeta `docs/play_store/`:

| Recurso | Formato | Dimensiones | Ubicación en el proyecto | Estado |
| :--- | :---: | :---: | :--- | :---: |
| **Icono de la aplicación en alta resolución** | PNG de 32 bits | 512 x 512 px (máx. 1 MB) | `docs/play_store/assets/app_icon_512x512.png` | ✅ Listo (desde SVG) |
| **Gráfico de funciones (*Feature Graphic*)** | PNG | 1024 x 500 px (máx. 15 MB) | `docs/play_store/assets/feature_graphic_1024x500.png` | ✅ Listo (capturas reales) |
| **Captura 01: Pantalla principal y favoritas** | PNG | 1080 x 2424 px | `docs/play_store/screenshots/01_pantalla_principal_hero.png` | ✅ Listo |
| **Captura 02: Interacción Tap-to-Base** | PNG | 1080 x 2424 px | `docs/play_store/screenshots/02_tap_to_base_lira_turca.png` | ✅ Listo |
| **Captura 03: Selector modal y buscador** | PNG | 1080 x 2424 px | `docs/play_store/screenshots/03_modal_bottom_sheet_gestion.png` | ✅ Listo |
| **Captura 04: Diálogo de créditos y autoría** | PNG | 1080 x 2424 px | `docs/play_store/screenshots/04_dialogo_creditos_juan_gestal.png` | ✅ Listo |
| **Icono vectorial maestro para marketing** | SVG escalable | Vectorial 512x512 | `docs/play_store/assets/app_icon.svg` | ✅ Listo |

---

## 3. Configuración de la ficha de Play Store

En la consola de Google Play, navega a **Crecimiento** ➔ **Presencia en Google Play Store** ➔ **Ficha de Play Store principal**:

1. **Detalles de la aplicación:**
   - **Nombre de la aplicación:** `Conversor de divisas y viaje` (Copiar de `ficha_play_store.md`).
   - **Descripción breve:** `Conversor de monedas rápido, sin conexión y con tipos de cambio oficiales del BCE.`
   - **Descripción completa:** Pegar el texto formateado de `ficha_play_store.md`.
2. **Traducciones adicionales:**
   - Añadir traducciones para **Inglés**, **Francés**, **Alemán** y **Portugués** copiando las secciones preparadas en `ficha_play_store.md`.
3. **Subida de gráficos:**
   - Arrastra el icono de 512x512 px, el gráfico de funciones de 1024x500 px y las 4 capturas de pantalla de móvil.

---

## 4. Clasificación y categorización de la app

En **Configuración de la tienda**:
* **Tipo de aplicación:** Aplicación.
* **Categoría:** *Finanzas* (o secundariamente *Viajes y guías locales*).
* **Etiquetas (*Tags*):** Finanzas personales, Divisas, Calculadora, Viajes.
* **Datos de contacto del desarrollador:**
  - Correo electrónico de soporte.
  - Sitio web: `https://gestal.es`

---

## 5. Cuestionarios obligatorios de contenido y políticas

En el menú lateral, accede a **Políticas y programas** ➔ **Contenido de la aplicación**:

### 5.1. Política de privacidad
* Enlace público obligatorio: Sube el contenido de `docs/play_store/politica_de_privacidad.md` a tu web o a un GitHub Gist público (por ejemplo: `https://gestal.es/privacidad-conversor`) y pega la URL en la consola.

### 5.2. Acceso a la aplicación
* Seleccionar: **«Todas las funciones están disponibles sin restricciones ni credenciales»** (la app no tiene login ni muros de pago).

### 5.3. Anuncios
* Seleccionar: **«No, mi aplicación no contiene anuncios»**.

### 5.4. Clasificación de contenido (Cuestionario IARC)
1. Introduce tu correo de contacto.
2. Selecciona la categoría **«Utilidades, productividad, finanzas u otras»**.
3. Responde **NO** a todas las preguntas de violencia, lenguaje soez, sustancias controladas, apuestas con dinero real o compras dentro de la app.
4. Resultado obtenido: **PEGI 3** (Europa) / **Everyone** (EE. UU.) / **Clasificación para todas las edades**.

### 5.5. Público objetivo y contenido
* Seleccionar rango de edad: **18 años o más** (o todas las edades).
* Marcar que la aplicación no está diseñada específicamente para niños.

### 5.6. Seguridad de los datos (*Data safety questionnaire*)
Google Play exige declarar con precisión qué datos se recogen y transmiten:
1. **¿Tu aplicación recoge o comparte algún tipo de datos de usuario obligatorios o secundarios?**
   - Seleccionar: **NO**.
2. **¿Se cifran todos los datos de usuario que recoge la aplicación durante la transmisión?**
   - Seleccionar: **SÍ** (las peticiones a la API de Frankfurter se realizan bajo el protocolo seguro HTTPS/TLS).
3. **¿Permite la aplicación que los usuarios soliciten la eliminación de sus datos?**
   - No aplica (al no recoger ningún dato personal).

### 5.7. Aplicaciones de servicios financieros
* Google pregunta si la app presta servicios bancarios, créditos o compraventa de criptomonedas.
* Marcar: **«Esta aplicación no ofrece préstamos, cuentas bancarias ni servicios de inversión regulados; es únicamente una herramienta utilitaria de consulta y cálculo de tipos de cambio oficiales del BCE»**.

---

## 6. Generación del binario oficial de producción (Android App Bundle)

### 6.1. Creación del almacén de claves criptográficas (*Keystore*)
Si aún no dispones de un almacén de claves para producción, genéralo desde la terminal:

```bash
keytool -genkey -v \
  -keystore conversor_release.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias conversor_key
```

> [!WARNING]
> Haz una copia de seguridad segura de este archivo `.jks` y sus contraseñas. Nunca lo subas a un repositorio público de Git.

### 6.2. Compilación del App Bundle firmado con Gradle
En la carpeta `00-Apps_Reales/02-Conversor_Divisas/app/`, ejecuta:

```bash
./gradlew bundleRelease
```

El archivo generado estará ubicado en:
`app/build/outputs/bundle/release/app-release.aab`

---

## 7. Subida del binario y envío a revisión

1. En Google Play Console, ve a **Lanzamiento** ➔ **Producción** (o **Pruebas cerradas** si tu cuenta lo requiere).
2. Haz clic en **Crear nueva versión**.
3. Sube el archivo `app-release.aab`.
4. En **Notas de la versión** (*Release Notes*), añade un resumen breve en español e inglés:
   ```text
   es-ES:
   Versión 1.0.0: Lanzamiento inicial del Conversor de divisas.
   • Cálculo reactivo e instantáneo con tipos de cambio oficiales del BCE.
   • Modo sin conexión (offline-first) y soporte multilingüe.
   • Interacción Tap-to-Base y reordenación de favoritas personalizada.
   • Diseñado en Jetpack Compose con animaciones fluidas y bajo licencia libre MIT.
   
   en-US:
   Version 1.0.0: Initial release of Currency Converter.
   • Reactive and instant calculations with official ECB exchange rates.
   • 100% offline-first resilience and full multi-language support.
   • Innovative Tap-to-Base interaction and customizable favorites.
   • Built with Jetpack Compose featuring smooth fluid animations.
   ```
5. Pulsa en **Guardar** ➔ **Revisar versión** ➔ **Iniciar el lanzamiento**.
6. El equipo de revisión de Google validará la app en un plazo habitual de 24 a 72 horas. ¡Tu aplicación estará publicada en todo el mundo!
