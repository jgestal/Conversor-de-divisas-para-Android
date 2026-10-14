# 💶 Paso 00: Requisitos, diseño de interacción y análisis de la API pública

En este tutorial construiremos una aplicación móvil real completa: un **conversor de divisas internacional** reactivo, fluido y diseñado específicamente para resolver las necesidades cotidianas del viajero.

---

## 1. Requisitos funcionales y de producto

La aplicación resuelve de forma instantánea las operaciones de cambio más habituales mediante cuatro pilares fundamentales:

1. **Interacción «Tap-to-Base» en pantalla principal:**
   - La divisa activa de cálculo se muestra en una tarjeta destacada superior (*Hero Card*) junto con el campo numérico editable.
   - Las demás divisas favoritas o frecuentes se muestran en tarjetas ordenadas en una lista inferior.
   - **Interacción reina:** Al tocar cualquier tarjeta de la lista de favoritas, esa divisa pasa de inmediato a la posición superior (*Hero*), invirtiendo el tipo de cambio y recalculando todas las demás monedas al instante.
2. **Reorganización y orden personalizado de favoritas:**
   - Cada tarjeta favorita cuenta con botones de desplazamiento hacia arriba y hacia abajo para permitir al usuario ordenar sus monedas preferidas según sus prioridades de viaje o consulta. El orden personalizado se preserva automáticamente entre aperturas de la app.
3. **Gestión ergonómica mediante `ModalBottomSheet` con selección en un solo toque:**
   - Un botón de acción («+ Gestionar») despliega una hoja inferior nativa de Material 3 con buscador en tiempo real (por nombre o código ISO).
   - **Selección instantánea:** Al pulsar directamente sobre cualquier divisa del catálogo, se añade o retira de favoritas de forma automática, sin interrumpir la navegación ni cerrar la hoja modal.
4. **Resiliencia sin conexión (*Offline-first*):**
   - Cuando se viaja a otro país es habitual encontrarse en el metro o en tránsito sin datos móviles ni conexión Wi-Fi. La aplicación guarda localmente las últimas tasas descargadas y la fecha/hora de sincronización oficial. Si la red no está disponible, la app sigue funcionando con normalidad mostrando un aviso discreto.
5. **Soporte multi-idioma (i18n) para mercados globales:**
   - Para maximizar su alcance en Google Play Store, la aplicación está completamente internacionalizada mediante catálogos de recursos de cadenas (`res/values/strings.xml`). Se proporciona soporte nativo en los idiomas de mayor demanda internacional (inglés por defecto, español, francés, alemán, portugués, italiano, turco y japonés).
6. **Pantalla de créditos y autoría:**
   - Diálogo accesible desde la barra superior con información del autor (**Juan Gestal**), foto, enlaces a su web y perfiles, y aviso de licencia abierta MIT.
7. **Iconografía nativa con emojis:**
   - Las banderas de los países se representan mediante emojis Unicode nativos (🇪🇺, 🇹🇷, 🇺🇸, 🇬🇧, 🇯🇵, etc.), eliminando la necesidad de importar decenas de imágenes vectoriales SVG o bibliotecas pesadas de carga de imágenes.

---

## 2. El modelo mental del viajero: ¿por qué «Tap-to-Base»?

El caso de uso principal de un conversor de divisas en movilidad es bidireccional:

* **Escenario A (Presupuesto propio):** *«Tengo 50 euros en el bolsillo, ¿cuántas liras turcas (TRY) son para saber si puedo comprar este recuerdo?»* (Base: EUR).
* **Escenario B (Precio local en destino):** *«En la carta de un restaurante en Estambul un plato cuesta 350 liras, ¿cuánto representa eso en mis euros?»* (Base: TRY).

Obligar al usuario a abrir un selector modal cada vez que quiere responder al Escenario B genera fricción innecesaria. Al permitir pulsar directamente sobre la tarjeta de TRY visible en la lista de favoritas, la moneda pasa a la cabecera activa y el usuario solo tiene que escribir `350` para ver al momento el coste en euros, dólares o libras.

```
┌────────────────────────────────────────────────────────┐
│                   Divisa activa (Hero)                 │
│  [ 🇹🇷 TRY ]          [ 350.00                        ] │
│  BCE: 14/10/2026 16:30 CET                             │
├────────────────────────────────────────────────────────┤
│  Favoritas (toca una para activarla)       [+ Gestionar]│
│                                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │ 🇪🇺 EUR • Euro                            6.33 € │  │
│  │ 1 TRY = 0.0181 EUR                            ⭐ │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │ 🇺🇸 USD • Dólar estadounidense           7.08 $ │  │
│  │ 1 TRY = 0.0202 USD                            ⭐ │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

---

## 3. Análisis de la fuente de datos: Frankfurter API

Para obtener los tipos de cambio utilizaremos **[Frankfurter API](https://api.frankfurter.dev/)**, un servicio web público, libre y de código abierto que publica las tasas oficiales de referencia del **Banco Central Europeo (BCE)**.

### Ventajas técnicas de esta API:
* **Cero claves ni registros (Zero API Key):** No requiere solicitar un token de acceso ni registrarse en ninguna plataforma. Cualquier persona puede clonar y ejecutar el proyecto sin configurar claves secretas en `local.properties`.
* **Datos oficiales fiables:** Las tasas se sincronizan todos los días laborables a las 16:00 CET directamente desde el Banco Central Europeo.
* **Alta disponibilidad y rapidez:** Peticiones HTTP directas bajo protocolo HTTPS con baja latencia.

> [!NOTE]
> **Decisión de arquitectura: ¿Por qué Frankfurter frente a servicios comerciales con API Key?**
> En el desarrollo de software móvil, las APIs que exigen registro suelen limitar el número de peticiones mensuales gratuitas (ej. 100 o 250 llamadas al mes) y revocan el servicio si se supera la cuota. Para una aplicación educativa o de código abierto, esto provocaría que los usuarios experimentaran fallos inesperados. Frankfurter opera como un bien público digital sobre infraestructura abierta, eliminando barreras de configuración inicial y garantizando reproducibilidad total.

### Consulta del endpoint:
Para obtener las tasas relativas a una divisa base (por ejemplo, el Euro o la Lira turca) se realiza una petición HTTP GET con el parámetro `base`:

```http
GET https://api.frankfurter.dev/v1/latest?base=EUR
```

### Respuesta JSON devuelta por el servidor:

```json
{
  "amount": 1.0,
  "base": "EUR",
  "date": "2026-10-14",
  "rates": {
    "AUD": 1.5971,
    "BRL": 5.6246,
    "CAD": 1.5918,
    "CHF": 0.933,
    "CNY": 7.4925,
    "GBP": 0.84385,
    "JPY": 177.35,
    "TRY": 55.2921,
    "USD": 1.0852
  }
}
```

---

## 4. Modelado eficiente con `Map<String, Double>`

El nodo `rates` del JSON devuelve un objeto con pares clave-valor donde cada clave es el código ISO 4217 de 3 letras de la moneda.

En lugar de definir una clase DTO rígida con 30 atributos (`val usd: Double, val gbp: Double, val try: Double...`), mapeamos este nodo directamente a un diccionario de Kotlin:

```kotlin
data class CambioRespuesta(
    @SerializedName("amount") val amount: Double,
    @SerializedName("base") val base: String,
    @SerializedName("date") val date: String,
    @SerializedName("rates") val rates: Map<String, Double>
)
```

### Ventajas de este enfoque:
1. **Resistencia a cambios:** Si el Banco Central Europeo añade una nueva divisa o deja de cotizar otra, la aplicación no se rompe ni requiere actualizar el modelo de datos.
2. **Búsqueda en tiempo constante O(1):** Consultar el tipo de cambio de cualquier moneda es tan directo como `rates["TRY"] ?: 0.0`.
3. **Código conciso:** La definición de datos queda resuelta en menos de 10 líneas de código.

> [!TIP]
> **Ventaja de tipado en Gson:** Cuando un servicio REST devuelve un objeto JSON cuyas claves son dinámicas (como códigos de países o monedas), mapearlo a `Map<String, Double>` es la mejor práctica en Kotlin y Android. Si se usaran propiedades fijas, cualquier divisa nueva devuelta por el BCE provocaría una excepción de deserialización o requeriría actualizar y recompilar la app.

---

## 5. Catálogo de divisas mundiales y banderas emoji

Para enriquecer la interfaz con el nombre completo de la moneda, su símbolo comercial y su bandera nacional, creamos un catálogo estático desacoplado de la API:

| Código | Nombre en español | Símbolo | Bandera emoji | País o región |
| :---: | :--- | :---: | :---: | :--- |
| **EUR** | Euro | € | 🇪🇺 | Unión Europea |
| **USD** | Dólar estadounidense | $ | 🇺🇸 | Estados Unidos |
| **GBP** | Libra esterlina | £ | 🇬🇧 | Reino Unido |
| **TRY** | Lira turca | ₺ | 🇹🇷 | Turquía |
| **JPY** | Yen japonés | ¥ | 🇯🇵 | Japón |
| **CHF** | Franco suizo | CHF | 🇨🇭 | Suiza |
| **CAD** | Dólar canadiense | CA$ | 🇨🇦 | Canadá |
| **AUD** | Dólar australiano | A$ | 🇦🇺 | Australia |
| **CNY** | Yuan chino | ¥ | 🇨🇳 | China |
| **INR** | Rupia india | ₹ | 🇮🇳 | India |
| **BRL** | Real brasileño | R$ | 🇧🇷 | Brasil |
| **MXN** | Peso mexicano | $ | 🇲🇽 | México |
| **SEK** | Corona sueca | kr | 🇸🇪 | Suecia |
| **NOK** | Corona noruega | kr | 🇳🇴 | Noruega |
| **DKK** | Corona danesa | kr | 🇩🇰 | Dinamarca |
| **PLN** | Zloty polaco | zł | 🇵🇱 | Polonia |
| **CZK** | Corona checa | Kč | 🇨🇿 | República Checa |
| **HUF** | Forinto húngaro | Ft | 🇭🇺 | Hungría |
| **ILS** | Nuevo séquel israelí | ₪ | 🇮🇱 | Israel |
| **KRW** | Won surcoreano | ₩ | 🇰🇷 | Corea del Sur |
| **SGD** | Dólar de Singapur | S$ | 🇸🇬 | Singapur |
| **NZD** | Dólar neozelandés | NZ$ | 🇳🇿 | Nueva Zelanda |
| **THB** | Baht tailandés | ฿ | 🇹🇭 | Tailandia |
| **ZAR** | Rand sudafricano | R | 🇿🇦 | Sudáfrica |

> [!NOTE]
> **¿Por qué banderas emoji nativas en vez de archivos SVG o PNG?**
> Cada bandera emoji es una secuencia estándar Unicode (Regional Indicator Symbols) renderizada directamente por la tipografía del sistema operativo Android (`Noto Color Emoji`). Esto aporta tres ventajas enormes:
> 1. **Reducción radical del peso del APK:** No añadimos decenas de imágenes vectoriales o bitmaps a la carpeta `res/drawable/`.
> 2. **Cero consumo de memoria de decodificación:** No se requiere una librería de carga de imágenes (como Coil o Glide) ni caché en disco.
> 3. **Escalabilidad vectorial perfecta:** Se ven nítidas en cualquier densidad de pantalla (mdpi, hdpi, xxhdpi, xxxhdpi) de forma nativa.

---

## 6. Resumen del paso

* Hemos definido los requisitos funcionales de la aplicación centrados en el caso de uso del viajero (interacción *Tap-to-Base*).
* Analizamos el servicio de Frankfurter API y justificamos el modelado dinámico con `Map<String, Double>`.
* Diseñamos un catálogo completo con banderas emoji para optimizar el rendimiento y peso del APK.

En el **[Paso 01](01_Configuracion_y_Capa_de_Red.md)** configuraremos el proyecto en Android Studio, añadiremos las dependencias y crearemos la capa de datos y red con Retrofit.

