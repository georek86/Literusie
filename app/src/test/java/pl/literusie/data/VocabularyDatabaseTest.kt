package pl.literusie.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.literusie.domain.model.WordCatalog
import java.io.File

/** Sprawdza prawdziwą bazę słów z assets: komplet pól, unikalność i obecność plików ilustracji. */
class VocabularyDatabaseTest {
    private val assets = File(System.getProperty("literusie.assets") ?: "src/main/assets")
    private val parsed by lazy { WordParser.parse(File(assets, "words_database.json").readText(Charsets.UTF_8)) }
    private val words get() = parsed.words

    @Test fun `baza wczytuje sie bez odrzuconych rekordow`() {
        assertEquals(emptyList<String>(), parsed.rejected)
        assertTrue("baza pusta", words.size >= 500)
    }

    @Test fun `identyfikatory slowa i ilustracje sa unikalne`() {
        assertEquals(words.size, words.map { it.id }.toSet().size)
        assertEquals(words.size, words.map { it.text }.toSet().size)
        assertEquals("kazde slowo ma wlasna ilustracje", words.size, words.map { it.imageId }.toSet().size)
    }

    @Test fun `slowa to wielkie polskie litery`() {
        val alphabet = "AĄBCĆDEĘFGHIJKLŁMNŃOÓPQRSŚTUVWXYZŹŻ".toSet()
        for (w in words) assertTrue("${w.id} ${w.text}", w.text.all { it in alphabet })
    }

    @Test fun `kazda ilustracja istnieje i jest poprawnym SVG`() {
        for (w in words) {
            val f = File(assets, "illustrations/${w.imageId}.svg")
            assertTrue("brak ${f.name} dla ${w.text}", f.isFile && f.length() > 100)
            assertTrue("${f.name} nie jest SVG", f.readText().contains("<svg"))
        }
    }

    @Test fun `poziomy trudnosci 1-4 sa uzyte i rosna z dlugoscia`() {
        assertEquals(setOf(1, 2, 3, 4), words.map { it.difficulty }.toSet())
        val avg = (1..4).map { l -> words.filter { it.difficulty == l }.map { it.text.length }.average() }
        assertTrue(avg.zipWithNext().all { (a, b) -> a < b })
    }

    @Test fun `kategorie i okladki`() {
        val c = WordCatalog(words)
        assertTrue(c.categories.size >= 20)
        for (cat in c.categories) assertTrue(cat.name, cat.wordCount >= 3)
        assertTrue(File(assets, "illustrations/${c.categories.first().coverImageId}.svg").isFile)
    }

    @Test fun `brak imion obcych i slow spoza polskiego zasobu dzieciecego`() {
        val banned = setOf("OSCAR", "JOHNSON", "BISHOP", "WASHINGTON", "JENNIFER", "PERCY", "MASON", "STANLEY", "TANNER", "DAVE", "LITTLE", "STAR", "EVA", "HAN", "BABY")
        assertTrue(words.map { it.text }.none { it in banned })
    }
}
