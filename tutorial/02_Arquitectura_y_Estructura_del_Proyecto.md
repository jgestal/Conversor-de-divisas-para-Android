# 📐 Paso 02: Arquitectura y estructura del proyecto

Antes de comenzar a escribir las clases de la aplicación, es fundamental comprender la **arquitectura del software** que vamos a construir. Una aplicación bien estructurada facilita la mantenibilidad, previene errores de ciclo de vida en Android y permite que cada componente tenga una única responsabilidad bien definida (*Single Responsibility Principle*).

En este capítulo analizaremos el patrón arquitectónico **MVVM** (*Model-View-ViewModel*), el flujo unidireccional de datos (*Unidirectional Data Flow - UDF*) y el mapa completo de paquetes y archivos que compondrán nuestra app.

---

## 1. Visión general de la arquitectura MVVM + UDF

Nuestra aplicación adopta la guía oficial de arquitectura para Android moderna recomendada por Google, dividiendo el código en tres capas principales:

```text
+-------------------------------------------------------------------------+
|                      CAPA DE PRESENTACIÓN (UI)                          |
|                                                                         |
|   +-----------------------+              +--------------------------+   |
|   |    MainActivity.kt    |  <------->   |   ConversorScreen.kt     |   |
|   |  (Punto de entrada)   |              |  (Compose + Animaciones) |   |
|   +-----------------------+              +--------------------------+   |
|                                                       |                 |
|                   +-----------------------------------+                 |
|                   |                                                     |
|                   v                                                     |
|   +-------------------------------+   +-----------------------------+   |
|   |       TarjetaDivisa.kt        |   |   ModalGestionDivisas.kt    |   |
|   | (Fila interactiva + reorden)  |   |  (BottomSheet + buscador)   |   |
|   +-------------------------------+   +-----------------------------+   |
+-------------------------------------------------------------------------+
                                    |                ^
                       Eventos de   |                |  Estado reactivo
                       interacción  |                |  StateFlow<UiState>
                                    v                |
+-------------------------------------------------------------------------+
|                  CAPA DE NEGOCIO Y ESTADO (VIEWMODEL)                   |
|                                                                         |
|                     +---------------------------+                       |
|                     |   ConversorViewModel.kt   |                       |
|                     |  - Cálculo a 60 FPS       |                       |
|                     |  - Gestión de favoritas   |                       |
|                     |  - Corrutinas / viewModel |                       |
|                     +---------------------------+                       |
|                                   |                                     |
|                                   v                                     |
|                     +---------------------------+                       |
|                     |    ConversorUiState.kt    |                       |
|                     |  (Estado único inmutable) |                       |
|                     +---------------------------+                       |
+-------------------------------------------------------------------------+
                                    |                ^
                     Peticiones y   |                |  Datos actualizados
                     persistencia   |                |  Tasas / Favoritos
                                    v                |
+-------------------------------------------------------------------------+
|                          CAPA DE DATOS (DATA)                           |
|                                                                         |
|   +----------------------------+      +-----------------------------+   |
|   |      DivisaModelos.kt      |      |     DivisasCatalogo.kt      |   |
|   |  - DivisaInfo (modelos)    |      |  - 24 divisas con banderas  |   |
|   |  - CambioRespuesta (DTO)   |      |  - Motor de búsqueda texto  |   |
|   +----------------------------+      +-----------------------------+   |
|                                                                         |
|   +----------------------------+      +-----------------------------+   |
|   |   PreferenciasDivisas.kt   |      |       DivisaApi.kt          |   |
|   |  - SharedPreferences       |      |  - Retrofit HTTP Client     |   |
|   |  - Orden en JSON (Offline) |      |  - API Frankfurter (BCE)    |   |
|   +----------------------------+      +-----------------------------+   |
+-------------------------------------------------------------------------+
                                                       |
                                                       v
                                            [ Red externa: API BCE ]
```

---

## 2. Flujo unidireccional de datos (*Unidirectional Data Flow*)

Para evitar estados inconsistentes entre lo que el usuario ve en la pantalla y lo que hay almacenado en memoria, aplicamos el patrón **UDF**:

```text
                 1. Usuario pulsa una divisa (Tap-to-Base)
               +----------------------------------------+
               |                                        |
               v                                        |
   +-----------------------+              +--------------------------+
   |   Interfaz de Usuario |              |   ConversorViewModel     |
   |   (Jetpack Compose)   |              |   (Lógica de negocio)    |
   +-----------------------+              +--------------------------+
               ^                                        |
               |                                        | 2. Ejecuta cálculo
               |                                        |    en memoria RAM
               |                                        v
               |                          +--------------------------+
               |                          |    Capa de Datos         |
               |                          |  (Guarda en SharedPreferences)
               |                          +--------------------------+
               |                                        |
               +----------------------------------------+
                  3. Emite nuevo ConversorUiState
                     (Compose se recompone suavemente)
```

1. **Los eventos viajan hacia arriba (UI ➔ ViewModel):** La interfaz visual es pasiva; cuando el usuario introduce un número, pulsa una tarjeta para convertirla en base (*Tap-to-Base*) o reordena favoritas, simplemente invoca una función del `ConversorViewModel`.
2. **El estado viaja hacia abajo (ViewModel ➔ UI):** El `ConversorViewModel` procesa la lógica, actualiza la persistencia local y emite un nuevo objeto inmutable `ConversorUiState` a través de un `StateFlow`. La interfaz de Compose observa este flujo y se actualiza de forma automática y reactiva.

---

## 3. Estructura de paquetes y responsabilidades

Organizamos el código fuente en paquetes semánticos dentro de `app/src/main/java/es/gestal/conversordivisas/`:

```text
es.gestal.conversordivisas/
│
├── MainActivity.kt
│    └── Punto de entrada de la app en Android.
│        Inicializa la API oficial de Splash Screen y establece ConversorScreen
│        dentro del contenedor setContent { ... }.
│
├── data/                                [CAPA DE DATOS Y RED]
│    ├── DivisaModelos.kt
│    │    └── Define CambioRespuesta (modelo para deserializar el JSON de la API)
│    │        y DivisaInfo (información de cada moneda con identificador de cadena).
│    │
│    ├── DivisasCatalogo.kt
│    │    └── Catálogo estático con 24 divisas mundiales, asignación de banderas
│    │        en formato emoji nativo y algoritmo de búsqueda multi-idioma.
│    │
│    ├── PreferenciasDivisas.kt
│    │    └── Gestor de persistencia ligera sobre SharedPreferences. Almacena la
│    │        divisa activa, el último importe y la lista ordenada en JSON (Offline-first).
│    │
│    └── DivisaApi.kt
│         └── Interfaz de endpoints con corrutinas (suspend fun) y cliente Singleton
│             de Retrofit con OkHttp, logging y tiempos de espera de 15 segundos.
│
└── ui/                                  [CAPA DE PRESENTACIÓN]
     ├── ConversorUiState.kt
     │    └── Estado inmutable de la pantalla (Single Source of Truth). Modela
     │        la divisa activa, la lista de conversiones, el estado de red y carga.
     │
     ├── ConversorViewModel.kt
     │    └── Cerebro reactivo de la aplicación. Maneja el recálculo aritmético
     │        instantáneo a 60 FPS, corrutinas de descarga y persistencia.
     │
     ├── ConversorScreen.kt
     │    └── Pantalla principal en Jetpack Compose. Ensambla el Scaffold, la cabecera,
     │        el campo de importe, la lista LazyColumn y el estado vacío.
     │
     ├── componentes/                    [COMPONENTES VISUALES REUTILIZABLES]
     │    ├── TarjetaDivisa.kt
     │    │    └── Tarjeta individual de cada divisa con controles de reordenación,
     │    │        animación de retirada y respuesta al toque.
     │    │
     │    ├── ModalGestionDivisas.kt
     │    │    └── Hoja inferior deslizable (ModalBottomSheet) para buscar entre
     │    │        las 24 monedas del catálogo y marcar/desmarcar favoritas.
     │    │
     │    └── DialogoCreditos.kt
     │         └── Ventana modal accesible desde la cabecera con la información
     │             de autoría de Juan Gestal, licencia MIT y créditos del BCE.
     │
     └── theme/                         [SISTEMA DE DISEÑO MATERIAL 3]
          ├── Color.kt
          │    └── Definición de colores principales (tonos Esmeralda, Pizarra y Menta).
          │
          ├── Theme.kt
          │    └── Configuración del MaterialTheme y adaptación a modo claro / oscuro.
          │
          └── Type.kt
               └── Escala tipográfica estándar de Compose.
```

---

## 4. Principios clave de diseño aplicados

### 1. Desacoplamiento total del modelo de datos (*Clean Domain*)
Las clases dentro de `data/` (`DivisaInfo`, `CambioRespuesta`) son clases de datos puras de Kotlin. No contienen referencias a `Activity`, `View` ni al ciclo de vida de Android. Esto evita fugas de memoria y permite realizar pruebas unitarias inmediatas en la máquina virtual de desarrollo sin necesidad de emuladores lentos.

### 2. Filosofía *Offline-first* (Resiliencia sin conexión)
Los viajeros utilizan con frecuencia los conversores de moneda en situaciones sin internet (aviones, aeropuertos internacionales, metro). Nuestra arquitectura nunca bloquea la interfaz ante la falta de conexión:
* Lee primero las tasas almacenadas previamente en `PreferenciasDivisas`.
* Permite operar y convertir con los últimos datos conocidos.
* Notifica de forma discreta al usuario mediante el banner de estado sin conexión.

### 3. Rendimiento y 60 FPS en Compose
* **Recálculo puramente aritmético:** El cambio de importe no consulta la red; multiplica instantáneamente en memoria el importe base por las tasas en caché.
* **Preservación de claves con `key`:** En las listas perezosas (`LazyColumn`), cada elemento utiliza como clave su código ISO único (`key = { it.info.codigo }`). Esto permite a Compose reutilizar nodos y animar recolocaciones fluidamente sin parpadeos.

---

## 5. Resumen del paso

* Establecimos el patrón **MVVM** dividiendo la aplicación en capas de Presentación, Negocio y Datos.
* Implementamos el flujo unidireccional de datos (**UDF**) para garantizar la consistencia del estado en todo momento.
* Mapeamos la totalidad de archivos y paquetes que componen la solución, definiendo la responsabilidad de cada clase.

En el **[Paso 03](03_Capa_de_Datos_y_Red.md)** comenzaremos la implementación práctica creando los modelos de datos, el catálogo de divisas, el gestor de preferencias y el cliente de red Retrofit.
