package es.gestal.conversordivisas.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
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

                        val colorEstrella by androidx.compose.animation.animateColorAsState(
                            targetValue = if (esFavorita) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 250),
                            label = "color_estrella_sheet"
                        )

                        Icon(
                            imageVector = if (esFavorita) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = if (esFavorita) {
                                stringResource(R.string.quitar_de_favoritas)
                            } else {
                                stringResource(R.string.anadir_a_favoritas)
                            },
                            tint = colorEstrella,
                            modifier = Modifier
                                .size(36.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

