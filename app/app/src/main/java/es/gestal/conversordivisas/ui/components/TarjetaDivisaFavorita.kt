package es.gestal.conversordivisas.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
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

            val colorEstrella by androidx.compose.animation.animateColorAsState(
                targetValue = if (item.esFavorita) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 250),
                label = "color_estrella"
            )

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
                    tint = colorEstrella,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

