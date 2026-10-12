package pl.literusie.domain.model

/** Jedno słowo z bazy. [text] to poprawna polska pisownia wielkimi literami (to, co widzi i wpisuje dziecko). */
data class Word(
    val id: String,
    val text: String,
    val category: String,
    val secondaryCategory: String? = null,
    val difficulty: Int,
    val imageId: String,
    val description: String? = null,
    val source: String? = null,
    val imageLicense: String? = null,
) {
    /** Tekst do syntezy mowy: małymi literami, żeby silnik TTS nie literował wyrazów pisanych wersalikami. */
    val spoken: String get() = text.lowercase(java.util.Locale("pl", "PL"))

    fun inCategory(name: String): Boolean = category == name || secondaryCategory == name
}

/** Kategoria widoczna na ekranie wyboru. */
data class CategoryInfo(val name: String, val wordCount: Int, val coverImageId: String)

/** Niezmienny katalog wszystkich słów aplikacji. */
class WordCatalog(val words: List<Word>) {
    private val byId = words.associateBy { it.id }

    val categories: List<CategoryInfo> = run {
        val names = words.map { it.category }.distinct()
        names.map { name ->
            val inCat = words.filter { it.inCategory(name) }
            val cover = COVERS[name]?.let { c -> inCat.firstOrNull { it.text == c } } ?: inCat.first()
            CategoryInfo(name, inCat.size, cover.imageId)
        }.sortedBy { CATEGORY_ORDER.indexOf(it.name).let { i -> if (i < 0) Int.MAX_VALUE else i } }
    }

    operator fun get(id: String): Word? = byId[id]

    companion object {
        /** Kolejność kategorii na ekranie: od najbliższych dziecku. */
        val CATEGORY_ORDER = listOf(
            "zwierzęta", "jedzenie i napoje", "owoce i warzywa", "ludzie i rodzina", "ciało człowieka",
            "dom i jego wyposażenie", "ubrania", "zabawki i zabawa", "pojazdy i transport",
            "przyroda i pogoda", "rośliny", "kolory i kształty", "liczby i matematyka", "czynności",
            "miejsca i budynki", "zawody", "szkoła i przedszkole", "sport i ruch", "emocje",
            "czas i pory roku", "bezpieczeństwo", "przeciwieństwa", "inne słowa",
        )
        private val COVERS = mapOf(
            "zwierzęta" to "KOT", "jedzenie i napoje" to "PIZZA", "owoce i warzywa" to "JABŁKO",
            "ludzie i rodzina" to "RODZINA", "ciało człowieka" to "OKO", "dom i jego wyposażenie" to "DRZWI",
            "ubrania" to "CZAPKA", "zabawki i zabawa" to "BALON", "pojazdy i transport" to "SAMOCHÓD",
            "przyroda i pogoda" to "SŁOŃCE", "rośliny" to "KWIAT", "kolory i kształty" to "GWIAZDA",
            "liczby i matematyka" to "TRZY", "czynności" to "BIEGAĆ", "miejsca i budynki" to "DOM",
            "zawody" to "STRAŻAK", "szkoła i przedszkole" to "KSIĄŻKA", "sport i ruch" to "PIŁKA",
            "emocje" to "RADOŚĆ", "czas i pory roku" to "ZEGAR", "bezpieczeństwo" to "UWAGA",
            "przeciwieństwa" to "GORĄCO", "inne słowa" to "GITARA",
        )
    }
}
