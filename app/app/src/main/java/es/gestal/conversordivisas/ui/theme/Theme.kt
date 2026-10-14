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
