package pl.literusie.ui.input

import androidx.compose.ui.focus.FocusRequester
import kotlinx.coroutines.delay

/**
 * Ustawia fokus, gdy element jest już w drzewie. W listach/siatkach (Lazy*) element pierwszej komórki
 * powstaje dopiero podczas układu, więc pojedyncze wywołanie requestFocus() w LaunchedEffect bywa za wcześnie
 * i fokus nigdzie nie trafia – na TV oznacza to ekran bez zaznaczonego elementu.
 */
suspend fun FocusRequester.requestWhenReady(attempts: Int = 40, pauseMs: Long = 50) {
    repeat(attempts) {
        val ok = try { requestFocus(); true } catch (e: IllegalStateException) { false }
        if (ok) return
        delay(pauseMs)
    }
}
