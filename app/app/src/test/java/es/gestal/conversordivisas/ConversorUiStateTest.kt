package es.gestal.conversordivisas

import es.gestal.conversordivisas.ui.ConversorUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversorUiStateTest {

    @Test
    fun testTieneDivisas_cuandoHayFavoritasYActiva_retornaTrue() {
        val state = ConversorUiState(
            divisaActiva = "EUR",
            favoritos = listOf("EUR", "USD", "GBP")
        )
        assertTrue(state.tieneDivisas)
    }

    @Test
    fun testTieneDivisas_cuandoFavoritasVacia_retornaFalse() {
        val state = ConversorUiState(
            divisaActiva = "",
            favoritos = emptyList()
        )
        assertFalse(state.tieneDivisas)
    }

    @Test
    fun testTieneDivisas_cuandoActivaEnBlanco_retornaFalse() {
        val state = ConversorUiState(
            divisaActiva = "",
            favoritos = listOf("USD")
        )
        assertFalse(state.tieneDivisas)
    }

    @Test
    fun testFavoritasConvertidas_excluyeDivisaActiva() {
        val state = ConversorUiState(
            divisaActiva = "EUR",
            importeBase = "100.00",
            favoritos = listOf("EUR", "USD", "GBP"),
            tasas = mapOf("USD" to 1.10, "GBP" to 0.85)
        )

        val convertidas = state.favoritasConvertidas
        assertEquals(2, convertidas.size)
        assertEquals("USD", convertidas[0].info.codigo)
        assertEquals(110.0, convertidas[0].importeConvertido, 0.001)
        assertEquals("GBP", convertidas[1].info.codigo)
        assertEquals(85.0, convertidas[1].importeConvertido, 0.001)
    }

    @Test
    fun testInfoDivisaActiva_conActivaValida_retornaMetadatosCorrectos() {
        val state = ConversorUiState(divisaActiva = "TRY")
        assertEquals("TRY", state.infoDivisaActiva.codigo)
        assertEquals("₺", state.infoDivisaActiva.simbolo)
        assertEquals("🇹🇷", state.infoDivisaActiva.bandera)
    }

    @Test
    fun testInfoDivisaActiva_conActivaVacia_retornaFallbackSeguro() {
        val state = ConversorUiState(divisaActiva = "")
        assertEquals("", state.infoDivisaActiva.codigo)
        assertEquals("🌐", state.infoDivisaActiva.bandera)
    }
}
