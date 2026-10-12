package pl.literusie.domain.text

/**
 * Normalizacja polskich liter do porównań: dziecko może pisać bez ogonków (Ż → Z, Ł → L ...).
 * Oryginalna pisownia jest zachowywana w danych – normalizacja służy tylko do porównywania.
 */
object PolishNormalizer {
    private const val PL = "ĄĆĘŁŃÓŚŹŻ"
    private const val ASCII = "ACELNOSZZ"

    /** Zwraca wielką literę bez ogonka. Znaków spoza polskiego alfabetu nie zmienia (poza wielkością liter). */
    fun normalizeChar(c: Char): Char {
        val up = c.uppercaseChar()
        val i = PL.indexOf(up)
        return if (i >= 0) ASCII[i] else up
    }

    fun normalizeWord(s: String): String = buildString(s.length) {
        for (c in s) if (!c.isWhitespace()) append(normalizeChar(c))
    }

    /** Czy wpisana litera jest akceptowana w miejscu oczekiwanej (zawsze po normalizacji obu stron). */
    fun matches(typed: Char, expected: Char): Boolean = normalizeChar(typed) == normalizeChar(expected)
}
