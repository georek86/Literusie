package pl.literusie

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.literusie.data.WordRepository
import pl.literusie.data.packs.PackInstaller
import pl.literusie.data.room.LiterusieDatabase
import pl.literusie.data.room.RoomProgressStore
import pl.literusie.domain.model.WordCatalog
import pl.literusie.media.AndroidSoundEffects
import pl.literusie.media.AndroidSpeaker
import pl.literusie.settings.AndroidSettings
import pl.literusie.ui.app.AppDeps
import pl.literusie.ui.app.LiterusieApp
import pl.literusie.ui.theme.LiterusieTheme
import pl.literusie.ui.theme.Palette

/** Manifest pakietów publikowany jako zasób najnowszego wydania na GitHubie. */
private const val PACKS_MANIFEST_URL = "https://github.com/georek86/Literusie/releases/latest/download/manifest.json"

class MainActivity : ComponentActivity() {
    private var speaker: AndroidSpeaker? = null
    private var effects: AndroidSoundEffects? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val settings = AndroidSettings(this)
        val speaker = AndroidSpeaker(this).also { this.speaker = it }
        val effects = AndroidSoundEffects(settings).also { this.effects = it }
        val store = RoomProgressStore(LiterusieDatabase.create(this).progressDao())
        val packsDir = java.io.File(filesDir, "packs")
        val repository = WordRepository(
            read = { assets.open("words_database.json").bufferedReader(Charsets.UTF_8).use { it.readText() } },
            packsDir = packsDir,
        )
        val packs = PackInstaller(PACKS_MANIFEST_URL, packsDir, ::openHttps)
        val font = FontFamily(
            Font(R.font.nunito_semibold, FontWeight.Normal),
            Font(R.font.nunito_semibold, FontWeight.SemiBold),
            Font(R.font.nunito_extrabold, FontWeight.Bold),
            Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
            Font(R.font.nunito_black, FontWeight.Black),
        )

        setContent {
            // Baza słów jest czytana poza głównym wątkiem; do tego czasu widać tylko tło.
            val catalog by produceState<WordCatalog?>(null) {
                value = withContext(Dispatchers.IO) { runCatching { repository.catalog() }.getOrNull() }
            }
            val loaded = catalog
            when {
                loaded == null -> LiterusieTheme(font) { Box(Modifier.fillMaxSize().background(Palette.Cream)) }
                loaded.words.isEmpty() -> LiterusieTheme(font) {
                    Box(Modifier.fillMaxSize().background(Palette.Cream).padding(48.dp), Alignment.Center) {
                        Text("Nie udało się wczytać słówek. Zainstaluj aplikację ponownie.")
                    }
                }
                else -> LiterusieApp(
                    deps = AppDeps(
                        catalog = loaded, store = store, speaker = speaker, sounds = effects,
                        settings = settings, openVoiceSettings = ::openVoiceSettings,
                        packs = packs, onPacksChanged = { runOnUiThread { recreate() } },
                    ),
                    font = font,
                    backHook = { enabled, onBack -> BackHandler(enabled, onBack) },
                )
            }
        }
    }

    /** Tylko https, jawne limity czasu; przekierowania (GitHub Releases → CDN) obsługuje HttpURLConnection. */
    private fun openHttps(url: String): java.io.InputStream {
        require(url.startsWith("https://"))
        val c = java.net.URL(url).openConnection() as java.net.HttpURLConnection
        c.connectTimeout = 15_000; c.readTimeout = 30_000; c.instanceFollowRedirects = true
        if (c.responseCode != 200) throw java.io.IOException("serwer odpowiedział ${c.responseCode}")
        return c.inputStream
    }

    /** Ustawienia mowy lub instalacja danych głosowych; różne telewizory obsługują różne akcje. */
    private fun openVoiceSettings() {
        val candidates = listOf(
            Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA),
            Intent("com.android.settings.TTS_SETTINGS"),
            Intent(Settings.ACTION_SETTINGS),
        )
        for (intent in candidates) {
            try { startActivity(intent); return } catch (_: Exception) { /* próbujemy następną */ }
        }
    }

    override fun onStop() {
        super.onStop()
        speaker?.stop()
    }

    override fun onDestroy() {
        speaker?.shutdown()
        effects?.release()
        super.onDestroy()
    }
}
