# 📱 Paso 05: Interfaz gráfica con Jetpack Compose y Material 3

En este paso construiremos una interfaz visual moderna, limpia y ergonómica utilizando **Jetpack Compose** y los componentes de **Material 3**. 

Estructuraremos la pantalla en componentes desacoplados, implementaremos la interacción *Tap-to-Base*, una hoja inferior deslizable (*ModalBottomSheet*) para gestionar el catálogo y un diálogo de créditos del autor.

---

## 1. Paleta de colores y tema visual

Definimos una paleta cromática basada en tonos verde esmeralda y menta que evocan el ámbito financiero y de viajes.

### Color.kt (`app/src/main/java/es/gestal/conversordivisas/ui/theme/Color.kt`)
```kotlin
package es.gestal.conversordivisas.ui.theme

import androidx.compose.ui.graphics.Color

val EsmeraldaPrimario = Color(0xFF0F766E)
val EsmeraldaSecundario = Color(0xFF14B8A6)
val EsmeraldaContenedor = Color(0xFFCCFBF1)
val EsmeraldaFondo = Color(0xFFF0FDFA)
val TextoOscuro = Color(0xFF134E4A)
val TextoSecundario = Color(0xFF64748B)
val BordeSuave = Color(0xFFE2E8F0)
```

### Theme.kt (`app/src/main/java/es/gestal/conversordivisas/ui/theme/Theme.kt`)
```kotlin
package es.gestal.conversordivisas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = EsmeraldaPrimario,
    secondary = EsmeraldaSecundario,
    primaryContainer = EsmeraldaContenedor,
    background = EsmeraldaFondo,
    surface = Color.White,
    onPrimary = Color.White,
    onSurface = TextoOscuro
)

private val EsquemaOscuro = darkColorScheme(
    primary = EsmeraldaSecundario,
    secondary = EsmeraldaContenedor,
    primaryContainer = EsmeraldaPrimario,
    background = Color(0xFF042F2E),
    surface = Color(0xFF115E59),
    onPrimary = Color(0xFF042F2E),
    onSurface = Color(0xFFF0FDFA)
)

@Composable
fun ConversorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) EsquemaOscuro else EsquemaClaro

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
```

---

## 2. Recurso gráfico del autor

Para la pantalla de créditos, crea el vector drawable `app/src/main/res/drawable/ic_avatar_juan_gestal.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="96dp"
    android:height="96dp"
    android:viewportWidth="96"
    android:viewportHeight="96">
    <path
        android:fillColor="#0F766E"
        android:pathData="M48,48m-48,0a48,48 0,1,1 96,0a48,48 0,1,1 -96,0" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M48,22c-7.73,0 -14,6.27 -14,14s6.27,14 14,14s14,-6.27 14,-14S55.73,22 48,22z" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M48,56c-13.33,0 -26,6.67 -26,20v4h52v-4C74,62.67 61.33,56 48,56z" />
</vector>
```

---

## 3. Componentes modulares

### 3.1. TarjetaHeroActiva.kt
Ubicación: `app/src/main/java/es/gestal/conversordivisas/ui/components/TarjetaHeroActiva.kt`

Presenta la divisa activa superior, el campo de texto numérico editable y el indicador de conexión/frescura oficial del BCE.

```kotlin
package es.gestal.conversordivisas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.gestal.conversordivisas.R
import es.gestal.conversordivisas.data.DivisaInfo

@Composable
fun TarjetaHeroActiva(
    info: DivisaInfo,
    importe: String,
    ultimaActualizacion: String,
    esOffline: Boolean,
    cargando: Boolean,
    onImporteCambiado: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.divisa_activa),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                if (esOffline) {
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
                } else if (ultimaActualizacion.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.padding(1.dp)
                        )
                        Text(
                            text = stringResource(R.string.bce_sincronizado, ultimaActualizacion),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = info.bandera, fontSize = 32.sp)
                        Column {
                            Text(
                                text = info.codigo,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(info.nombreRes),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = importe,
                    onValueChange = onImporteCambiado,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    }
}
```

---

### 3.2. TarjetaDivisaFavorita.kt
Ubicación: `app/src/main/java/es/gestal/conversordivisas/ui/components/TarjetaDivisaFavorita.kt`

Implementa la interacción *Tap-to-Base*. Al pulsar en cualquier punto de la tarjeta, emite el evento para convertirse en la divisa activa.

```kotlin
package es.gestal.conversordivisas.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.gestal.conversordivisas.R
import es.gestal.conversordivisas.ui.ItemDivisaConvertida
import java.util.Locale

/**
 * Tarjeta de divisa favorita con interacción «Tap-to-Base» y controles de reordenación.
 * Al pulsar en el cuerpo de la tarjeta, la divisa se convierte en la divisa activa superior.
 * Los botones de flechas permiten desplazar la posición de la moneda en la lista.
 */
@Composable
fun TarjetaDivisaFavorita(
    item: ItemDivisaConvertida,
    divisaActiva: String,
    onSeleccionarComoActiva: (String) -> Unit,
    onToggleFavorita: (String) -> Unit,
    onMoverArriba: () -> Unit,
    onMoverAbajo: () -> Unit,
    puedeSubir: Boolean,
    puedeBajar: Boolean,
    modifier: Modifier = Modifier
) {
    val nombreLocalizado = stringResource(item.info.nombreRes)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSeleccionarComoActiva(item.info.codigo) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Bandera emoji nativa
            Text(text = item.info.bandera, fontSize = 32.sp)

            // Datos descriptivos de la divisa y tipo unitario
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.info.codigo,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• $nombreLocalizado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "1 %s = %.4f %s", divisaActiva, item.tasa, item.info.codigo),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = stringResource(R.string.toca_para_activar),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Importe convertido en tiempo real
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.US, "%,.2f", item.importeConvertido),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.info.simbolo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }

            // Botones compactos de reordenación arriba / abajo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onMoverArriba,
                    enabled = puedeSubir,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = stringResource(R.string.mover_arriba),
                        tint = if (puedeSubir) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onMoverAbajo,
                    enabled = puedeBajar,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.mover_abajo),
                        tint = if (puedeBajar) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Botón de favorita (estrella)
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
                    tint = if (item.esFavorita) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
```

---

### 3.3. BottomSheetGestionDivisas.kt
Ubicación: `app/src/main/java/es/gestal/conversordivisas/ui/components/BottomSheetGestionDivisas.kt`

Hoja inferior deslizable nativa con buscador en vivo para gestionar el catálogo y las divisas favoritas con una sola mano.

```kotlin
package es.gestal.conversordivisas.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.gestal.conversordivisas.R
import es.gestal.conversordivisas.data.DivisaInfo

/**
 * Hoja inferior deslizante (ModalBottomSheet) para buscar y gestionar divisas favoritas.
 * Al pulsar en cualquier divisa de la lista, se añade o retira de las favoritas automáticamente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetGestionDivisas(
    divisas: List<DivisaInfo>,
    favoritos: List<String>,
    divisaActiva: String,
    queryBusqueda: String,
    onQueryCambiada: (String) -> Unit,
    onToggleFavorita: (String) -> Unit,
    onCerrar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.gestionar_divisas_titulo),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Buscador reactivo en tiempo real
            OutlinedTextField(
                value = queryBusqueda,
                onValueChange = onQueryCambiada,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.buscar_divisa_placeholder)) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (queryBusqueda.isNotEmpty()) {
                        IconButton(onClick = { onQueryCambiada("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lista filtrada de divisas
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(divisas, key = { it.codigo }) { divisa ->
                    val esFavorita = favoritos.contains(divisa.codigo)
                    val esActiva = divisa.codigo == divisaActiva
                    val nombreDivisa = stringResource(divisa.nombreRes)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Al pinchar en la fila, se convierte automáticamente en favorita (o alterna)
                                onToggleFavorita(divisa.codigo)
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(text = divisa.bandera, fontSize = 30.sp)

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = divisa.codigo,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (esActiva) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (esActiva) {
                                    Text(
                                        text = "• ${stringResource(R.string.activa_tag)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = "$nombreDivisa (${divisa.simbolo})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }

                        IconButton(onClick = { onToggleFavorita(divisa.codigo) }) {
                            Icon(
                                imageVector = if (esFavorita) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = if (esFavorita) {
                                    stringResource(R.string.quitar_de_favoritas)
                                } else {
                                    stringResource(R.string.anadir_a_favoritas)
                                },
                                tint = if (esFavorita) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}
```

---

### 3.4. DialogoCreditos.kt
Ubicación: `app/src/main/java/es/gestal/conversordivisas/ui/components/DialogoCreditos.kt`

Diálogo de autoría con foto de **Juan Gestal**, enlaces a su web personal y licencia MIT:

```kotlin
package es.gestal.conversordivisas.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.gestal.conversordivisas.R

/**
 * Diálogo de créditos y autoría con foto de Juan Gestal, enlaces web y licencia MIT.
 */
@Composable
fun DialogoCreditos(
    onCerrar: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onCerrar,
        shape = RoundedCornerShape(24.dp),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Foto / Avatar de Juan Gestal
                Image(
                    painter = painterResource(id = R.drawable.ic_avatar_juan_gestal),
                    contentDescription = stringResource(R.string.desarrollado_por),
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )

                Text(
                    text = stringResource(R.string.creditos_titulo),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.desarrollado_por),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.descripcion_creditos),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )

                Text(
                    text = stringResource(R.string.fuente_datos_creditos),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Enlaces a la web personal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { uriHandler.openUri("https://gestal.es") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Web",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(text = "gestal.es", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { uriHandler.openUri("https://github.com/jgestal") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "GitHub", fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) {
                Text(stringResource(R.string.cerrar))
            }
        }
    )
}
```

---

### 3.5. PantallaSinDivisas.kt (Estado vacío / Empty State)
Ubicación: `app/src/main/java/es/gestal/conversordivisas/ui/components/PantallaSinDivisas.kt`

Cuando el usuario desmarca todas las divisas de su lista de favoritas, o al iniciar la app sin selección previa, evitamos mostrar una interfaz en blanco o rota mediante esta pantalla de bienvenida con un icono de respiración continua (*breathing pulse*) y un botón de llamada a la acción:

```kotlin
package es.gestal.conversordivisas.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.gestal.conversordivisas.R

/**
 * Pantalla vacía (Empty State) que se muestra cuando no hay ninguna divisa de referencia seleccionada.
 * Proporciona una bienvenida atractiva con microanimación de escala y llamada a la acción para elegir monedas.
 */
@Composable
fun PantallaSinDivisas(
    onAbrirGestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "latido_vacio")
    val escalaIcono by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala_icono"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Contenedor circular con icono animado
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(escalaIcono)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CurrencyExchange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Título de estado vacío
                Text(
                    text = stringResource(R.string.sin_divisas_titulo),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Texto explicativo
                Text(
                    text = stringResource(R.string.sin_divisas_descripcion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Botón principal de llamada a la acción
                Button(
                    onClick = onAbrirGestion,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.seleccionar_divisas_boton),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
```

---

## 4. Ensamblado de la pantalla principal: ConversorScreen.kt

Ubicación: `app/src/main/java/es/gestal/conversordivisas/ui/ConversorScreen.kt`:

```kotlin
package es.gestal.conversordivisas.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.gestal.conversordivisas.R
import es.gestal.conversordivisas.ui.components.BottomSheetGestionDivisas
import es.gestal.conversordivisas.ui.components.DialogoCreditos
import es.gestal.conversordivisas.ui.components.PantallaSinDivisas
import es.gestal.conversordivisas.ui.components.TarjetaDivisaFavorita
import es.gestal.conversordivisas.ui.components.TarjetaHeroActiva

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversorScreen(
    viewModel: ConversorViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val catalogoFiltrado = remember(state.queryBusqueda) {
        viewModel.obtenerCatalogoFiltrado(context)
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Botón con rotación animada continua durante la recarga de tasas
                    IconButton(
                        onClick = { viewModel.refrescar() },
                        enabled = !state.cargando && state.tieneDivisas
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

                    // Botón de información y créditos
                    IconButton(onClick = { viewModel.setMostrarDialogoCreditos(true) }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.creditos_y_autor)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            AnimatedContent(
                targetState = state.tieneDivisas,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "transicion_pantalla_vacia"
            ) { tieneDivisas ->
                if (tieneDivisas) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Tarjeta Hero de la divisa activa
                        TarjetaHeroActiva(
                            info = state.infoDivisaActiva,
                            importe = state.importeBase,
                            ultimaActualizacion = state.ultimaActualizacion,
                            esOffline = state.esOffline,
                            cargando = state.cargando,
                            onImporteCambiado = { viewModel.onImporteCambiado(it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Cabecera de la sección de favoritas y botón de gestión
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.favoritas_titulo),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                fontWeight = FontWeight.SemiBold
                            )

                            TextButton(onClick = { viewModel.setMostrarBottomSheetGestion(true) }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(stringResource(R.string.gestionar))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Lista de favoritas con interacción Tap-to-Base, reordenación y animación
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
                    }
                } else {
                    // Pantalla atractiva si no hay monedas de referencia
                    PantallaSinDivisas(
                        onAbrirGestion = { viewModel.setMostrarBottomSheetGestion(true) }
                    )
                }
            }

            // Hoja modal inferior para gestionar el catálogo completo
            if (state.mostrarBottomSheetGestion) {
                BottomSheetGestionDivisas(
                    divisas = catalogoFiltrado,
                    favoritos = state.favoritos,
                    divisaActiva = state.divisaActiva,
                    queryBusqueda = state.queryBusqueda,
                    onQueryCambiada = { viewModel.setQueryBusqueda(it) },
                    onToggleFavorita = { viewModel.toggleFavorita(it) },
                    onCerrar = { viewModel.setMostrarBottomSheetGestion(false) }
                )
            }

            // Diálogo de créditos y autoría de Juan Gestal
            if (state.mostrarDialogoCreditos) {
                DialogoCreditos(
                    onCerrar = { viewModel.setMostrarDialogoCreditos(false) }
                )
            }
        }
    }
}
```

> [!NOTE]
> **Memoización de cálculos con `remember`:**
> `val catalogoFiltrado = remember(state.queryBusqueda) { ... }` asegura que la lista de divisas solo se filtre cuando el usuario teclea en el buscador. Cuando el usuario edita el importe numérico de la divisa activa, la recomposición de la pantalla no vuelve a recalcular el filtrado de divisas, ahorrando ciclos de CPU.

> [!TIP]
> **Ajuste automático al teclado con `Modifier.imePadding()`:**
> Al aplicar `imePadding()` en el contenedor principal `Box`, Jetpack Compose eleva automáticamente el contenido cuando el teclado en pantalla aparece (*Input Method Editor*). Esto garantiza que el campo numérico y las tarjetas no queden ocultos detrás del teclado virtual.

---

## 5. Entrada de la aplicación: MainActivity.kt

Ubicación: `app/src/main/java/es/gestal/conversordivisas/MainActivity.kt`:

```kotlin
package es.gestal.conversordivisas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import es.gestal.conversordivisas.ui.ConversorScreen
import es.gestal.conversordivisas.ui.theme.ConversorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicializar la API oficial de Splash Screen ANTES de super.onCreate
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConversorTheme {
                ConversorScreen()
            }
        }
    }
}
```

> [!IMPORTANT]
> **Ubicación obligatoria de `installSplashScreen()`:**
> `installSplashScreen()` debe invocarse estrictamente **antes** de `super.onCreate(savedInstanceState)`. Si se llama después, la actividad ya habrá comenzado a inflar su tema base y el sistema arrojará un error de inicialización o mostrará un destello blanco.

---

## 6. Resumen del paso

* Hemos construido una interfaz ergonómica con interacción *Tap-to-Base*.
* Añadimos controles de reordenación interactiva (flechas arriba y abajo) en las tarjetas de favoritas para personalizar la lista.
* Implementamos un *ModalBottomSheet* deslizante con buscador reactivo y selección instantánea de favoritas al tocar cada fila.
* Diseñamos una pantalla de bienvenida (*Empty State*) con microanimación de latido para cuando no haya monedas seleccionadas.
* Integramos la API oficial de Splash Screen en `MainActivity.kt` con transición suave hacia Compose.
* Toda la interfaz está completamente internacionalizada mediante `stringResource(...)` para adaptarse al idioma del usuario.
* Incorporamos una pantalla de créditos que da visibilidad a la autoría de Juan Gestal y la licencia MIT.
* El ajuste de teclado con `imePadding` evita que los campos numéricos queden ocultos durante la escritura.

En el **[Paso 06](06_Animaciones_y_Microinteracciones_en_Compose.md)** profundizaremos en el diseño de movimiento en Compose: desaparición suave de tarjetas con `animateItem`, física de resortes, transiciones mecánicas de aeropuerto con `AnimatedContent` y rotación continua en GPU.
