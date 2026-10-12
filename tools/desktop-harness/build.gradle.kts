import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("org.jetbrains.compose") version "1.6.11"
}


val appMain = file("../../app/src/main/java")
val appTest = file("../../app/src/test/java")

sourceSets {
    main {
        kotlin.srcDir(appMain)
        // Pliki specyficzne dla Androida nie wchodzą do wersji desktopowej (mają tu własne odpowiedniki lub atrapy).
        val androidOnly = listOf("MainActivity.kt", "data/room/", "media/AndroidSpeaker.kt", "settings/", "ui/platform/")
        kotlin.exclude { el ->
            val path = el.file.invariantSeparatorsPath
            path.contains("/app/src/main/java/") && androidOnly.any { path.contains("/pl/literusie/$it") }
        }
    }
    test { kotlin.srcDir(appTest) }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation("org.json:json:20240303")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    testImplementation(compose.uiTest)
}

tasks.test {
    useJUnit()
    systemProperty("literusie.assets", file("../../app/src/main/assets").absolutePath)
    systemProperty("literusie.shots", layout.buildDirectory.dir("shots").get().asFile.absolutePath)
    testLogging { events("passed", "failed", "skipped"); showStandardStreams = false }
}

// Pakiety androidx.* są tylko na Google Maven. Compose Multiplatform publikuje ich kopie w Maven Central –
// podmieniamy zależności, żeby harness budował się też w środowiskach bez dostępu do Google Maven.
configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("androidx.annotation:annotation")).using(module("org.jetbrains.compose.annotation-internal:annotation:1.6.11"))
    }
    exclude(group = "androidx.lifecycle")
    exclude(group = "androidx.arch.core")
}
