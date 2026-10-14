# 🛡️ Política de privacidad de Conversor de divisas

*Última actualización: 14 de octubre de 2026*

Esta política de privacidad describe cómo la aplicación móvil **Conversor de divisas** (desarrollada por **Juan Gestal**) trata la información de sus usuarios.

---

## 🇪🇸 Versión en español

### 1. Recopilación de información
**Conversor de divisas no recopila, almacena, transmite ni comparte ningún dato personal identificable de sus usuarios.**

La aplicación no requiere registro, creación de cuentas, inicio de sesión ni acceso a identificadores personales, nombres, correos electrónicos, números de teléfono ni información bancaria.

### 2. Permisos del dispositivo
La aplicación solicita exclusivamente el siguiente permiso estándar del sistema operativo:
* `android.permission.INTERNET`: Necesario de manera exclusiva para conectar con la API pública y abierta de Frankfurter (`api.frankfurter.app`) y descargar las tasas oficiales de tipos de cambio del Banco Central Europeo (BCE).

La aplicación **NO solicita ni tiene acceso a:**
* Ubicación geográfica (GPS o red).
* Cámara ni micrófono.
* Contactos, agenda ni llamadas.
* Archivos personales, fotos o almacenamiento externo del dispositivo.

### 3. Almacenamiento local en el dispositivo
Para permitir el funcionamiento sin conexión a Internet (*Offline-first*), la aplicación almacena de forma local en el dispositivo del usuario (mediante `SharedPreferences` privadas de Android):
* La lista ordenada de monedas favoritas elegidas por el usuario.
* El último importe numérico introducido.
* La divisa base activa seleccionada.
* Las últimas tasas oficiales descargadas y la fecha de sincronización.

Esta información permanece estrictamente en el dispositivo del usuario y se elimina automáticamente al desinstalar la aplicación.

### 4. Servicios de terceros
La aplicación consulta la API pública de tipos de cambio de **Frankfurter** (servicio gratuito y de código abierto que publica los datos del Banco Central Europeo). Las peticiones HTTP envían únicamente el código de divisa base (por ejemplo, `EUR` o `TRY`) para recibir las tasas de cambio vigentes. No se envían cabeceras de seguimiento ni datos del usuario.

La aplicación **NO utiliza:**
* Redes publicitarias (AdMob, Unity Ads, etc.).
* Servicios de análisis de telemetría (Google Analytics for Firebase, Mixpanel, etc.).
* Bibliotecas de seguimiento (*trackers*) de terceros.

### 5. Menores de edad
Al no recopilar datos de ningún tipo, la aplicación es completamente segura para usuarios de todas las edades (clasificación PEGI 3 / Everyone).

### 6. Contacto
Si tienes cualquier consulta sobre esta política de privacidad o sobre el funcionamiento de la aplicación, puedes ponerte en contacto con el desarrollador:
* **Autor:** Juan Gestal
* **Sitio web:** [gestal.es](https://gestal.es)

---

## 🇬🇧 English version

### 1. Information collection
**Currency Converter does not collect, store, transmit, or share any personally identifiable information from its users.**

No registration, user account, or login is required. The application has no access to names, emails, phone numbers, or payment data.

### 2. Device permissions
The application requests only one standard system permission:
* `android.permission.INTERNET`: Strictly used to query the public and open Frankfurter API (`api.frankfurter.app`) to fetch official exchange rates published by the European Central Bank (ECB).

The application **DOES NOT request or access:**
* Geolocation (GPS or network).
* Camera or microphone.
* Contacts, address book, or phone state.
* Personal files, photos, or external device storage.

### 3. Local on-device storage
To enable seamless offline functionality, the app saves locally on the device (using Android's private `SharedPreferences`):
* The user's custom favorite currencies and order.
* The last entered conversion amount.
* The currently active base currency.
* The most recent exchange rates and sync timestamp.

This data never leaves your device and is erased upon uninstalling the application.

### 4. Third-party services
Exchange rates are synchronized via the public open-source Frankfurter API. HTTP requests contain only the requested currency code (e.g. `from=EUR`). The application contains **zero advertisements and zero telemetry/tracking SDKs**.

### 5. Contact
For any questions regarding this privacy policy, please contact:
* **Developer:** Juan Gestal
* **Website:** [gestal.es](https://gestal.es)
