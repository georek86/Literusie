package pl.literusie.ui.input

/**
 * Odfiltrowuje autopowtarzanie klawiatury: przytrzymany klawisz daje JEDNO zdarzenie „wciśnięty”,
 * dopóki nie zostanie zwolniony. Dzięki temu dziecko przytrzymujące „A” nie wpisze „AAAAA”.
 */
class HeldKeys<K> {
    private val held = HashSet<K>()

    /** @return true tylko dla pierwszego wciśnięcia (nie dla powtórzeń). */
    fun down(key: K): Boolean = held.add(key)

    fun up(key: K) { held.remove(key) }

    fun clear() = held.clear()
}
