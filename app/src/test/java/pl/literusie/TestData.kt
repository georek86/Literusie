package pl.literusie

import pl.literusie.domain.model.Word

/** Wspólne dane testowe. */
fun w(id: String, text: String, category: String = "zwierzęta", level: Int = 1, image: String = "e_$id", second: String? = null) =
    Word(id, text, category, second, level, image)

/** Pięć obowiązkowych słów z polskimi znakami. */
val CMA = w("cma", "ĆMA")
val ZABA = w("zaba", "ŻABA")
val LODZ = w("lodz", "ŁÓDŹ")
val GES = w("ges", "GĘŚ")
val ZREBAK = w("zrebak", "ŹREBAK")
