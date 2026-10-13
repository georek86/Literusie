package pl.literusie.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pl.literusie.data.packs.PackInstaller
import pl.literusie.data.packs.PackResult
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PackInstallerTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun zip(vararg files: Pair<String, String>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { z -> for ((n, c) in files) { z.putNextEntry(ZipEntry(n)); z.write(c.toByteArray()); z.closeEntry() } }
        return out.toByteArray()
    }

    private val words = """[{"id":"p1_1","word":"pies","category":"zwierzęta","difficulty":1,"imageId":"a_1"}]"""
    private fun goodZip() = zip("words.json" to words, "img/a_1.png" to "PNG")

    private fun manifest(bytes: ByteArray, sha: String = PackInstaller.sha256(bytes), url: String = "https://x/p.zip", size: Long = bytes.size.toLong()) =
        """{"packs":[{"id":"p1","version":1,"url":"$url","sha256":"$sha","size":$size,"wordCount":1}]}"""

    private fun installer(manifest: String, pack: ByteArray, dir: File = tmp.newFolder()) =
        dir to PackInstaller("https://x/manifest.json", dir) { u ->
            when (u) { "https://x/manifest.json" -> manifest.byteInputStream(); "https://x/p.zip" -> pack.inputStream(); else -> throw IOException("404") }
        }

    @Test fun `poprawny pakiet jest instalowany i wczytywany do bazy`() = runBlocking {
        val z = goodZip()
        val (dir, inst) = installer(manifest(z), z)
        assertEquals(PackResult.Installed(1), inst.update())
        assertTrue(File(dir, "p1/img/a_1.png").isFile)
        assertEquals(PackResult.UpToDate, inst.update())
        val cat = WordRepository({ "[]" }, dir).catalog()
        assertEquals("pack:p1/a_1", cat.words.single().imageId)
    }

    @Test fun `zla suma kontrolna - nic nie jest instalowane`() = runBlocking {
        val z = goodZip()
        val (dir, inst) = installer(manifest(z, sha = "0".repeat(64)), z)
        assertTrue(inst.update() is PackResult.Failed)
        assertFalse(File(dir, "p1").exists())
    }

    @Test fun `zip-slip i niedozwolone pliki sa odrzucane`() = runBlocking {
        for (bad in listOf("../evil.json", "img/../../x.png", "script.sh", "/abs.json")) {
            val z = zip("words.json" to words, bad to "x")
            val (dir, inst) = installer(manifest(z), z)
            assertTrue(bad, inst.update() is PackResult.Failed)
            assertFalse(File(dir, "p1").exists())
        }
    }

    @Test fun `tylko https i poprawny manifest`() = runBlocking {
        val z = goodZip()
        assertTrue(installer(manifest(z, url = "http://x/p.zip"), z).second.update() is PackResult.Failed)
        assertTrue(installer("to nie json", z).second.update() is PackResult.Failed)
        assertTrue(installer(manifest(z, size = 5), z).second.update() is PackResult.Failed)
    }

    @Test fun `blad sieci nie rzuca wyjatku`() = runBlocking {
        val inst = PackInstaller("https://x/m.json", tmp.newFolder()) { throw IOException("brak sieci") }
        assertTrue(inst.update() is PackResult.Failed)
    }

    @Test fun `duplikaty z baza podstawowa i slowa bez obrazka sa pomijane`() {
        val dir = tmp.newFolder()
        File(dir, "p1/img").mkdirs()
        File(dir, "p1/words.json").writeText(
            """[{"id":"p1_1","word":"pies","category":"zwierzęta","difficulty":1,"imageId":"a_1"},
               {"id":"p1_2","word":"kot","category":"zwierzęta","difficulty":1,"imageId":"a_2"},
               {"id":"p1_3","word":"lis","category":"zwierzęta","difficulty":1,"imageId":"a_3"}]"""
        )
        File(dir, "p1/img/a_1.png").writeText("x"); File(dir, "p1/img/a_2.png").writeText("x")
        val base = """[{"id":"w1","word":"kot","category":"zwierzęta","difficulty":1,"imageId":"e_1"}]"""
        val repo = WordRepository({ base }, dir)
        assertEquals(listOf("KOT", "PIES"), repo.catalog().words.map { it.text })
        assertTrue(repo.rejected.any { "lis".uppercase() in it.uppercase() })
    }
}
