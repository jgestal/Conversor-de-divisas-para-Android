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

                        // Lista de favoritas con interacción Tap-to-Base, reordenación y animación de desaparición/desplazamiento
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

