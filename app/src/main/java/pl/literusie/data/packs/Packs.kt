package pl.literusie.data.packs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.ZipInputStream

/** Opis jednego pakietu słów z manifestu (plik manifest.json w wydaniu GitHub Releases). */
data class PackInfo(val id: String, val version: Int, val url: String, val sha256: String, val size: Long, val wordCount: Int)

sealed interface PackResult {
    data class Installed(val packs: Int) : PackResult
    data object UpToDate : PackResult
    data class Failed(val reason: String) : PackResult
}

/** Co widzi interfejs: jedna operacja „pobierz/zaktualizuj dodatkowe słówka”. */
interface PackService {
    suspend fun update(): PackResult
}

object PackLimits {
    const val MAX_MANIFEST = 1L shl 20
    const val MAX_PACK = 80L shl 20
    const val MAX_UNPACKED = 250L shl 20
    const val MAX_ENTRIES = 4000
    val ID = Regex("^[a-z0-9_]{1,32}$")
    val ENTRY = Regex("^(words\\.json|img/[A-Za-z0-9_]{1,64}\\.png)$")
    val HEX64 = Regex("^[0-9a-f]{64}$")
}

object PackManifestParser {
    /** Zwraca poprawne pakiety; każdy błąd w manifeście powoduje odrzucenie całości (ufamy tylko w pełni poprawnym danym). */
    fun parse(json: String): List<PackInfo> {
        val arr = JSONObject(json).getJSONArray("packs")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val p = PackInfo(
                o.getString("id"), o.getInt("version"), o.getString("url"),
                o.getString("sha256").lowercase(), o.getLong("size"), o.optInt("wordCount", 0),
            )
            require(PackLimits.ID.matches(p.id)) { "zły id pakietu" }
            require(p.url.startsWith("https://")) { "adres pakietu musi być https" }
            require(PackLimits.HEX64.matches(p.sha256)) { "zły skrót sha256" }
            require(p.size in 1..PackLimits.MAX_PACK) { "zły rozmiar pakietu" }
            p
        }
    }
}

/**
 * Pobiera pakiety do [dir]/<id>/ (words.json oraz pliki PNG w img/). Zasady bezpieczeństwa: tylko https, limit rozmiaru,
 * zgodność SHA-256 przed rozpakowaniem, rozpakowanie wyłącznie plików z białej listy (bez ścieżek względnych),
 * atomowa podmiana katalogu. Każdy błąd kończy się [PackResult.Failed] – nigdy wyjątkiem w górę.
 */
class PackInstaller(
    private val manifestUrl: String,
    private val dir: File,
    private val fetch: (String) -> InputStream,
) : PackService {

    override suspend fun update(): PackResult = withContext(Dispatchers.IO) {
        try {
            require(manifestUrl.startsWith("https://")) { "adres manifestu musi być https" }
            val manifest = fetch(manifestUrl).use { readLimited(it, PackLimits.MAX_MANIFEST) }.toString(Charsets.UTF_8)
            val packs = PackManifestParser.parse(manifest)
            var installed = 0
            for (p in packs) if (installedVersion(p.id) != p.version) { install(p); installed++ }
            if (installed == 0) PackResult.UpToDate else PackResult.Installed(installed)
        } catch (e: Exception) {
            PackResult.Failed(e.message ?: e.javaClass.simpleName)
        }
    }

    private fun installedVersion(id: String): Int =
        runCatching { File(dir, "$id/version.txt").readText().trim().toInt() }.getOrDefault(-1)

    private fun install(p: PackInfo) {
        val tmp = File(dir, ".tmp_${p.id}").apply { deleteRecursively(); mkdirs() }
        try {
            val bytes = fetch(p.url).use { readLimited(it, p.size).toByteArray() }
            check(bytes.size.toLong() == p.size) { "rozmiar pakietu ${p.id} się nie zgadza" }
            check(sha256(bytes) == p.sha256) { "suma kontrolna pakietu ${p.id} się nie zgadza" }
            unzipSafely(bytes, tmp)
            check(File(tmp, "words.json").isFile) { "pakiet ${p.id} nie ma words.json" }
            File(tmp, "version.txt").writeText(p.version.toString())
            val target = File(dir, p.id)
            target.deleteRecursively()
            check(tmp.renameTo(target)) { "nie udało się zapisać pakietu ${p.id}" }
        } finally {
            tmp.deleteRecursively()
        }
    }

    companion object {
        fun sha256(bytes: ByteArray): String =
            MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

        fun readLimited(input: InputStream, limit: Long): ByteArrayOutputStream {
            val out = ByteArrayOutputStream()
            val buf = ByteArray(16 * 1024)
            var total = 0L
            while (true) {
                val n = input.read(buf)
                if (n < 0) break
                total += n
                if (total > limit) throw IOException("dane większe niż dozwolone ($limit B)")
                out.write(buf, 0, n)
            }
            return out
        }

        fun unzipSafely(zip: ByteArray, into: File) {
            var entries = 0
            var unpacked = 0L
            ZipInputStream(zip.inputStream()).use { zis ->
                while (true) {
                    val e = zis.nextEntry ?: break
                    if (e.isDirectory) continue
                    check(++entries <= PackLimits.MAX_ENTRIES) { "za dużo plików w pakiecie" }
                    check(PackLimits.ENTRY.matches(e.name)) { "niedozwolony plik w pakiecie: ${e.name}" }
                    val data = readLimited(zis, PackLimits.MAX_UNPACKED - unpacked)
                    unpacked += data.size()
                    val out = File(into, e.name)
                    check(out.canonicalPath.startsWith(into.canonicalPath + File.separator)) { "ścieżka poza katalogiem" }
                    out.parentFile.mkdirs()
                    out.writeBytes(data.toByteArray())
                }
            }
        }
    }
}

/** Atrapa do testów i podglądu. */
class FakePackService(var result: PackResult = PackResult.UpToDate) : PackService {
    var calls = 0
    override suspend fun update(): PackResult { calls++; return result }
}
