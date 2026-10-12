package pl.literusie.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordParserTest {
    private fun rec(id: String = "w1", word: String = "kot", cat: String = "zwierzęta", lvl: Int = 1, img: String = "e_1") =
        """{"id":"$id","word":"$word","category":"$cat","difficulty":$lvl,"imageId":"$img"}"""

    @Test fun `poprawny rekord`() {
        val r = WordParser.parse("[${rec()}]")
        assertEquals(1, r.words.size)
        assertEquals("KOT", r.words[0].text)
        assertTrue(r.rejected.isEmpty())
    }

    @Test fun `bledne rekordy sa odrzucane z opisem, reszta dziala`() {
        val json = "[" + listOf(
            rec(),
            rec(id = "", word = "pies"), rec(id = "w3", word = ""), rec(id = "w4", word = "a b"),
            rec(id = "w5", word = "ryba", cat = ""), rec(id = "w6", word = "mysz", img = ""),
            rec(id = "w7", word = "lew", lvl = 9), rec(id = "w1", word = "sowa"), rec(id = "w9", word = "KOT"),
            "42",
        ).joinToString(",") + "]"
        val r = WordParser.parse(json)
        assertEquals(listOf("KOT"), r.words.map { it.text })
        assertEquals(9, r.rejected.size)
    }

    @Test fun `uszkodzony plik nie rzuca wyjatku`() {
        val r = WordParser.parse("{ to nie jest json")
        assertTrue(r.words.isEmpty())
        assertEquals(1, r.rejected.size)
    }
}
