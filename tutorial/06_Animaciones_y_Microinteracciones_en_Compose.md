# ✨ Paso 06: Animaciones y microinteracciones en Jetpack Compose

Una interfaz estática que actualiza sus elementos de forma instantánea puede resultar funcional, pero a menudo produce saltos visuales bruscos que desorientan al usuario. En el diseño de experiencias de usuario (*UX/UI*) modernas, las animaciones y microinteracciones no son meros adornos visuales: cumplen un rol cognitivo esencial al proporcionar **continuidad espacial**, **confirmación de estados** y **ritmo táctil**.

En este paso enriqueceremos el conversor de divisas con transiciones estratégicas y microinteracciones de alto impacto visual y bajo consumo de recursos, aprovechando las capacidades nativas de **Jetpack Compose**.

---

## 1. Principios de diseño de movimiento (*motion design*) en Compose

Al diseñar animaciones para una herramienta de viaje y finanzas, aplicamos tres reglas fundamentales:

1. **Propósito funcional frente a artificio:** Cada animación debe responder a una acción explícita del usuario o a un cambio de estado del sistema (por ejemplo, confirmar que una tarjeta ha dejado de ser favorita o que una divisa ha ascendido a la posición activa).
2. **Duraciones ágiles (entre 200 y 350 ms):** En una herramienta de cálculo rápido, animaciones excesivamente lentas generan frustración. Las transiciones deben sentirse instantáneas y reactivas.
3. **Aceleración por hardware (GPU):** Siempre que sea posible, animamos propiedades gráficas como escala, alfa y rotación a través de capas de dibujo (`graphicsLayer`), evitando recomposiciones innecesarias del árbol de nodos de Compose.

---

## 2. Desaparición fluida de tarjetas al quitar de favoritas con `Modifier.animateItem()`

### 2.1. El problema de los saltos bruscos en listas dinámicas
Cuando el usuario pulsa en la estrella dorada de una tarjeta para retirarla de favoritas, el elemento se elimina del estado `state.favoritasConvertidas`. Sin animación, la tarjeta desaparece de golpe en 0 milisegundos y todas las tarjetas inferiores dan un salto instantáneo hacia arriba. Este comportamiento es tosco y puede hacer que el usuario pierda el hilo visual de lo que acaba de ocurrir.

### 2.2. Implementación con la API moderna de Compose
A partir del BOM moderno de Compose, el modificador `Modifier.animateItem()` sustituye a la antigua función `animateItemPlacement()`, permitiendo configurar de manera independiente tres fases del ciclo de vida del elemento dentro de `LazyItemScope`:

* **`fadeOutSpec` (Desaparición al quitar de favoritas):** Define cómo se desvanece la tarjeta al eliminarse del listado. Configuramos una interpolación de opacidad suave de 300 ms (`tween(durationMillis = 300)`).
* **`fadeInSpec` (Aparición al marcar como favorita):** Aplica un rebote elástico agradable cuando entra un nuevo elemento (`spring(dampingRatio = Spring.DampingRatioMediumBouncy)`).
* **`placementSpec` (Desplazamiento y reordenación física):** Aplica física de resortes elásticos (*spring physics*) cuando las demás tarjetas se desplazan para ocupar el hueco libre o cuando el usuario utiliza los botones de subir/bajar posición.

> [!IMPORTANT]
> Para que Compose pueda identificar qué elemento entra, sale o cambia de posición, es imprescindible proporcionar una clave estable y única en `itemsIndexed` mediante el parámetro `key`:
> ```kotlin
> key = { _, item -> item.info.codigo }
> ```

Veamos cómo queda integrado en el `LazyColumn` de `ConversorScreen.kt`:

```kotlin
// Lista de favoritas con interacción Tap-to-Base, reordenación y animación de desaparición
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(bottom = 16.dp)
) {
    itemsIndexed(
        items = state.favoritasConvertidas,
        key = { _, item -> item.info.codigo }
    ) { index, item ->
        TarjetaDivisaFavorita(
            item = item,
            divisaActiva = state.divisaActiva,
            onSeleccionarComoActiva = { viewModel.seleccionarDivisaActiva(it) },
            onToggleFavorita = { viewModel.toggleFavorita(it) },
            onMoverArriba = { viewModel.moverFavoritaArriba(item.info.codigo) },
            onMoverAbajo = { viewModel.moverFavoritaAbajo(item.info.codigo) },
            puedeSubir = index > 0,
            puedeBajar = index < state.favoritasConvertidas.size - 1,
            modifier = Modifier.animateItem(
                fadeInSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                fadeOutSpec = tween(durationMillis = 300),
                placementSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        )
    }
}
```

Al desmarcar la estrella o pulsar sobre la divisa en el catálogo, la tarjeta se desvanece suavemente mientras las tarjetas inferiores se deslizan hacia arriba con una amortiguación física natural.

---

## 3. Transición de panel de aeropuerto en la tarjeta activa con `AnimatedContent`

### 3.1. Metáfora visual: el panel de salidas y llegadas
Cuando el usuario pulsa sobre cualquier moneda de la lista de favoritas (interacción *Tap-to-Base*), la tarjeta superior (*Hero Card*) cambia de divisa base (por ejemplo, de EUR a TRY). 

Para reforzar visualmente la idea de que la divisa se ha transformado, aplicamos un efecto inspirado en los paneles mecánicos de llegadas y salidas de aeropuertos y estaciones de tren: la bandera y el código anteriores se deslizan hacia arriba desvaneciéndose, mientras que los datos de la nueva divisa entran deslizándose desde abajo.

### 3.2. Implementación técnica
Utilizamos el contenedor `AnimatedContent` asociándolo a `info.codigo`. Cada vez que cambia el código de divisa activa, Compose ejecuta la transición de contenido definida por el operador `togetherWith`:

```kotlin
AnimatedContent(
    targetState = info,
    transitionSpec = {
        (slideInVertically(
            animationSpec = tween(durationMillis = 280),
            initialOffsetY = { height -> height / 2 }
        ) + fadeIn(animationSpec = tween(durationMillis = 280)))
            .togetherWith(
                slideOutVertically(
                    animationSpec = tween(durationMillis = 280),
                    targetOffsetY = { height -> -height / 2 }
                ) + fadeOut(animationSpec = tween(durationMillis = 280))
            )
    },
    label = "transicion_hero_divisa"
) { targetInfo ->
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = targetInfo.bandera,
            fontSize = 38.sp
        )
        Column {
            Text(
                text = targetInfo.codigo,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(targetInfo.nombreRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}
```

---

## 4. Expansión elástica del aviso sin conexión con `AnimatedVisibility`

Cuando el dispositivo pierde la conectividad a Internet y opera con las tasas guardadas en local, la tarjeta Hero muestra una pastilla de aviso (*badge*).

Para evitar que este aviso aparezca o desaparezca de forma repentina alterando la altura de los textos adyacentes, empleamos `AnimatedVisibility` con una combinación de expansión horizontal y desvanecimiento:

```kotlin
AnimatedVisibility(
    visible = esOffline,
    enter = fadeIn(animationSpec = tween(durationMillis = 250)) + expandHorizontally(),
    exit = fadeOut(animationSpec = tween(durationMillis = 200)) + shrinkHorizontally()
) {
    Row(
        modifier = Modifier
            .background(Color(0xFFFEF3C7), shape = CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = stringResource(R.string.sin_conexion),
            tint = Color(0xFFD97706),
            modifier = Modifier.padding(1.dp)
        )
        Text(
            text = stringResource(R.string.sin_conexion),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF92400E),
            fontWeight = FontWeight.SemiBold
        )
    }
}
```

> [!NOTE]
> **El operador `togetherWith` en `AnimatedContent`:**
> A partir de las versiones recientes de Compose Animation, el operador infijo `togetherWith` reemplaza al antiguo `with`. Permite combinar de forma declarativa y tipada la animación de entrada (`ContentTransform`) con la animación de salida, asegurando que ambos elementos coexistan durante los milisegundos de la transición sin cortes de fotogramas.

---

## 5. Microinteracción de color táctil con `animateColorAsState`

Tanto en las tarjetas de favoritas como en el catálogo modal, la estrella de favoritos proporciona retroalimentación instantánea. Para que el cambio de color no sea inmediato, interpolamos suavemente el matiz entre el amarillo dorado (`Color(0xFFEAB308)`) y el gris translúcido del tema mediante `animateColorAsState`:

```kotlin
val colorEstrella by animateColorAsState(
    targetValue = if (item.esFavorita) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
    animationSpec = tween(durationMillis = 250),
    label = "color_estrella"
)

IconButton(
    onClick = { onToggleFavorita(item.info.codigo) },
    modifier = Modifier.size(36.dp)
) {
    Icon(
        imageVector = if (item.esFavorita) Icons.Filled.Star else Icons.Outlined.StarOutline,
        contentDescription = if (item.esFavorita) {
            stringResource(R.string.quitar_de_favoritas)
        } else {
            stringResource(R.string.anadir_a_favoritas)
        },
        tint = colorEstrella
    )
}
```

---

## 6. Rotación continua acelerada por hardware con `rememberInfiniteTransition`

En la barra de herramientas superior (`TopAppBar`), el botón de recarga de tasas cuenta con una animación continua durante las operaciones de sincronización en segundo plano.

En lugar de crear un bucle manual con corrutinas que desencadene recomposiciones constantes, utilizamos la API de transiciones infinitas de Compose combinada con `Modifier.graphicsLayer`:

```kotlin
val infiniteTransition = rememberInfiniteTransition(label = "rotacion_refresco")
val anguloRotacion by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 850, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    ),
    label = "angulo_giro"
)

IconButton(
    onClick = { viewModel.refrescar() },
    enabled = !state.cargando
) {
    Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = stringResource(R.string.actualizar_tasas),
        modifier = if (state.cargando) {
            Modifier.graphicsLayer { rotationZ = anguloRotacion }
        } else {
            Modifier
        }
    )
}
```

> [!TIP]
> Al aplicar `rotationZ` dentro de la lambda del modificador `graphicsLayer`, la rotación se delega directamente a la GPU en la fase de dibujo (*draw phase*), logrando 60 / 120 FPS estables con cero sobrecarga en la CPU.

---

## 7. Promoción animada de la siguiente favorita y transición a pantalla vacía

### 7.1. Promoción automática de la divisa de referencia
Un caso de uso crucial en la experiencia de usuario ocurre cuando el viajero decide retirar de favoritas la divisa que en ese momento actúa como divisa base de referencia (la que se muestra en la tarjeta Hero superior):

1. Al desmarcar la divisa activa, el `ViewModel` la retira de `favoritos` y selecciona automáticamente la **siguiente divisa favorita** (la primera de la lista restante) como nueva moneda activa.
2. Esta actualización del estado desencadena inmediatamente la transición `AnimatedContent` de la tarjeta Hero: el código y bandera salientes se deslizan hacia arriba desvaneciéndose, mientras que la nueva divisa entra deslizándose desde abajo.
3. Simultáneamente, la divisa desmarcada desaparece de la lista inferior con `fadeOutSpec`, y la divisa promocionada se traslada a la tarjeta superior, garantizando una continuidad visual impecable.

### 7.2. Pantalla de estado vacío con microanimación de escala
¿Qué ocurre si el usuario desmarca todas las divisas favoritas o si la aplicación arranca sin monedas de referencia configuradas?

En lugar de mostrar una interfaz rota o vacía, la pantalla principal conmuta de forma suave a `PantallaSinDivisas`:

```kotlin
AnimatedContent(
    targetState = state.tieneDivisas,
    transitionSpec = {
        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
    },
    label = "transicion_pantalla_vacia"
) { tieneDivisas ->
    if (tieneDivisas) {
        // Tarjeta Hero activa + Lista de favoritas ordenadas
        ...
    } else {
        // Pantalla atractiva si no hay monedas de referencia
        PantallaSinDivisas(
            onAbrirGestion = { viewModel.setMostrarBottomSheetGestion(true) }
        )
    }
}
```

En `PantallaSinDivisas`, el icono central incorpora un efecto de respiración continuo (*breathing pulse*) mediante `rememberInfiniteTransition` y `Modifier.scale`, invitando amablemente al usuario a pulsar el botón «Seleccionar divisas» para abrir el catálogo y fijar sus primeras monedas.

> [!TIP]
> **Transición sin saltos a pantallas vacías con `AnimatedContent`:**
> El uso de un simple `if (tieneDivisas)` condicional provocaría un corte instantáneo en el árbol visual al retirar la última divisa. Al envolver la bifurcación en `AnimatedContent(targetState = state.tieneDivisas)`, Compose interpola la desaparición del listado y la aparición gradual de la ilustración de bienvenida simultáneamente con un fundido cruzado (`fadeIn + fadeOut`), transmitiendo una sensación de acabado extremadamente pulido.

---

## 8. Resumen del paso

* Implementamos la desaparición suave de tarjetas al desmarcarlas de favoritas con `Modifier.animateItem(fadeOutSpec = tween(300))`.
* Diseñamos la promoción automática de la siguiente divisa favorita cuando se retira la moneda de referencia, activando la transición de panel de salidas.
* Creamos una pantalla de estado vacío (*Empty State*) con animación de latido suave y llamada a la acción hacia el catálogo.
* Aplicamos física de resortes elásticos para que el resto de tarjetas se reubiquen armónicamente al cambiar de orden o al suprimir un elemento.
* Mostramos el estado sin conexión mediante la expansión lateral fluida de `AnimatedVisibility`.
* Añadimos retroalimentación táctil de color con `animateColorAsState` y rotación GPU continua con `rememberInfiniteTransition`.

En el **[Paso 07](07_Preparacion_y_Publicacion_Play_Store.md)** completaremos el proyecto configurando el almacén de claves criptográficas, firmando el App Bundle (`.aab`) y preparando el lanzamiento oficial en Google Play Store.
