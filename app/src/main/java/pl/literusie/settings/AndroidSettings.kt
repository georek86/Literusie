package pl.literusie.settings

import android.content.Context
import pl.literusie.media.SettingsStore

class AndroidSettings(context: Context) : SettingsStore {
    private val prefs = context.getSharedPreferences("literusie_settings", Context.MODE_PRIVATE)

    override var soundEffects: Boolean
        get() = prefs.getBoolean("effects", false)
        set(v) { prefs.edit().putBoolean("effects", v).apply() }

    override var hints: Boolean
        get() = prefs.getBoolean("hints", true)
        set(v) { prefs.edit().putBoolean("hints", v).apply() }
}
