package pl.slowomistrz.app.domain.normalizer

object PolishCharNormalizer {

    /**
     * Normalizuje polskie znaki diakrytyczne do znaków łacińskich:
     * Ą -> A, Ć -> C, Ę -> E, Ł -> L, Ń -> N, Ó -> O, Ś -> S, Ź -> Z, Ż -> Z
     * Zamienia małe litery na wielkie.
     */
    fun normalizeChar(char: Char): Char {
        return when (char.uppercaseChar()) {
            'Ą' -> 'A'
            'Ć' -> 'C'
            'Ę' -> 'E'
            'Ł' -> 'L'
            'Ń' -> 'N'
            'Ó' -> 'O'
            'Ś' -> 'S'
            'Ź', 'Ż' -> 'Z'
            else -> char.uppercaseChar()
        }
    }

    /**
     * Normalizuje cały ciąg znaków (usuwa spacje, zamienia polskie litery, wielkie litery).
     */
    fun normalizeWord(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (!ch.isWhitespace()) {
                sb.append(normalizeChar(ch))
            }
        }
        return sb.toString()
    }

    /**
     * Sprawdza, czy wpisany przez dziecko znak odpowiada oczekiwanemu znakowi na danej pozycji słowa.
     * Zwraca true jeśli znaki są identyczne lub jeśli ich znormalizowane formy są równe.
     * Np. wpisane 'Z' pasuje do 'Ż' lub 'Ź'.
     * Wpisane 'Ż' również pasuje do 'Ż'.
     * Wpisane 'C' pasuje do 'Ć'.
     */
    fun isCharMatch(inputChar: Char, expectedChar: Char): Boolean {
        if (inputChar.uppercaseChar() == expectedChar.uppercaseChar()) {
            return true
        }
        return normalizeChar(inputChar) == normalizeChar(expectedChar)
    }
}
