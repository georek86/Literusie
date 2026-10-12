package pl.slowomistrz.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.first
import pl.slowomistrz.app.data.local.SlowomistrzDatabase
import pl.slowomistrz.app.data.repository.WordRepository
import pl.slowomistrz.app.domain.engine.LearningSessionManager
import pl.slowomistrz.app.domain.model.WordItem
import pl.slowomistrz.app.media.PolishTtsManager
import pl.slowomistrz.app.ui.screens.CategorySelectionScreen
import pl.slowomistrz.app.ui.screens.LearningScreen
import pl.slowomistrz.app.ui.screens.MainMenuScreen
import pl.slowomistrz.app.ui.screens.ProgressScreen
import pl.slowomistrz.app.ui.theme.SlowomistrzTheme

enum class AppNavState {
    LOADING,
    MAIN_MENU,
    LEARNING,
    CATEGORIES,
    PROGRESS
}

class MainActivity : ComponentActivity() {

    private lateinit var ttsManager: PolishTtsManager
    private lateinit var database: SlowomistrzDatabase
    private lateinit var repository: WordRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ttsManager = PolishTtsManager(this)
        database = SlowomistrzDatabase.getDatabase(this)
        repository = WordRepository(this)

        setContent {
            SlowomistrzTheme {
                MainAppHost(
                    repository = repository,
                    database = database,
                    ttsManager = ttsManager
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}

@Composable
fun MainAppHost(
    repository: WordRepository,
    database: SlowomistrzDatabase,
    ttsManager: PolishTtsManager
) {
    var navState by remember { mutableStateOf(AppNavState.LOADING) }
    var allWords by remember { mutableStateOf<List<WordItem>>(emptyList()) }
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    var sessionManager by remember { mutableStateOf<LearningSessionManager?>(null) }

    val masteredCount by database.wordProgressDao().getMasteredCount().collectAsState(initial = 0)
    val practicedCount by database.wordProgressDao().getPracticedCount().collectAsState(initial = 0)

    LaunchedEffect(Unit) {
        val loaded = repository.getAllWords()
        val cats = repository.getCategories()
        allWords = loaded
        categories = cats
        navState = AppNavState.MAIN_MENU
    }

    when (navState) {
        AppNavState.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        AppNavState.MAIN_MENU -> {
            MainMenuScreen(
                onStartLearning = {
                    sessionManager = LearningSessionManager(
                        words = allWords,
                        progressDao = database.wordProgressDao()
                    )
                    navState = AppNavState.LEARNING
                },
                onChooseCategory = {
                    navState = AppNavState.CATEGORIES
                },
                onRepeatLearned = {
                    // Tryb powtórki: powtarzamy słowa
                    sessionManager = LearningSessionManager(
                        words = allWords,
                        progressDao = database.wordProgressDao()
                    )
                    navState = AppNavState.LEARNING
                },
                onViewProgress = {
                    navState = AppNavState.PROGRESS
                }
            )
        }
        AppNavState.LEARNING -> {
            val session = sessionManager
            if (session != null) {
                LearningScreen(
                    sessionManager = session,
                    ttsManager = ttsManager,
                    onBackToMenu = { navState = AppNavState.MAIN_MENU }
                )
            } else {
                navState = AppNavState.MAIN_MENU
            }
        }
        AppNavState.CATEGORIES -> {
            CategorySelectionScreen(
                categories = categories,
                onSelectCategory = { cat ->
                    val filtered = allWords.filter { it.category == cat }
                    sessionManager = LearningSessionManager(
                        words = filtered,
                        progressDao = database.wordProgressDao()
                    )
                    navState = AppNavState.LEARNING
                },
                onBack = { navState = AppNavState.MAIN_MENU }
            )
        }
        AppNavState.PROGRESS -> {
            ProgressScreen(
                masteredCount = masteredCount,
                practicedCount = practicedCount,
                totalWordsCount = allWords.size,
                onBack = { navState = AppNavState.MAIN_MENU }
            )
        }
    }
}
