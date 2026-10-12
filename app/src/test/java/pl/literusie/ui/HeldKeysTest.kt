package pl.literusie.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.literusie.ui.input.HeldKeys

class HeldKeysTest {
    @Test fun `autopowtarzanie jest ignorowane do zwolnienia klawisza`() {
        val k = HeldKeys<Char>()
        assertTrue(k.down('A'))
        repeat(10) { assertFalse(k.down('A')) }
        k.up('A')
        assertTrue(k.down('A'))
    }

    @Test fun `rozne klawisze sa niezalezne`() {
        val k = HeldKeys<Char>()
        assertTrue(k.down('A')); assertTrue(k.down('B')); assertFalse(k.down('A'))
    }
}
