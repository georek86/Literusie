package pl.slowomistrz.app.domain.model

enum class DifficultyLevel(val level: Int, val label: String) {
    LEVEL_1(1, "Bardzo łatwe (krótkie słowa)"),
    LEVEL_2(2, "Łatwe (codzienne słownictwo)"),
    LEVEL_3(3, "Średnie (dłuższe i trudniejsze głoski)"),
    LEVEL_4(4, "Zaawansowane (złożone pojęcia)")
}

data class WordItem(
    val id: String,
    val word: String, // Poprawna pisownia, np. "ŻABA"
    val displayWord: String = word,
    val category: String,
    val secondaryCategory: String? = null,
    val difficulty: DifficultyLevel,
    val imageId: String,
    val description: String? = null,
    val source: String? = null
)
