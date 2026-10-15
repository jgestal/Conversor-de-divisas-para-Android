package es.gestal.conversordivisas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = AzulMarinoProfundo,
    secondary = OroMoneda,
    primaryContainer = AzulMarinoContenedor,
    background = AzulMarinoFondo,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onPrimaryContainer = AzulMarinoProfundo,
    onSurface = TextoOscuro
)

private val EsquemaOscuro = darkColorScheme(
    primary = OroMonedaBrillante,
    secondary = PlataMoneda,
    primaryContainer = AzulMarinoSuperficie,
    background = AzulMarinoNoche,
    surface = AzulMarinoProfundo,
    onPrimary = AzulMarinoNoche,
    onSecondary = AzulMarinoNoche,
    onPrimaryContainer = Color.White,
    onSurface = Color(0xFFF1F5F9)
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
