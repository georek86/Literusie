// Zastępnik androidx.collection do testów na JVM.
// Prawdziwy artefakt jest tylko na Google Maven (niedostępnym w niektórych środowiskach). JetBrains publikuje
// jego źródła (Apache-2.0) w Maven Central – kompilujemy je tutaj lokalnie. Źródeł nie przechowujemy w repozytorium.
plugins { kotlin("multiplatform") version "1.9.24" }

group = "androidx.collection"
version = "1.4.0"

val sources by configurations.creating
dependencies { sources("org.jetbrains.compose.collection-internal:collection:1.6.11:sources@jar") }

val unpack = tasks.register<Copy>("unpackCollectionSources") {
    from({ zipTree(sources.singleFile) })
    into(layout.buildDirectory.dir("src"))
    // Kotlin 2.0 nie pozwala na @JvmField w deklaracjach expect – usuwamy je z tych dwóch plików.
    doLast {
        for (f in listOf("LongSparseArray.kt", "SparseArrayCompat.kt")) {
            val file = layout.buildDirectory.file("src/commonMain/androidx/collection/$f").get().asFile
            file.writeText(file.readText().lines().filterNot { it.trim() == "@JvmField" || it.trim().startsWith("@JvmSynthetic") }.joinToString("\n"))
        }
    }
}

kotlin {
    jvm()
    sourceSets {
        all { languageSettings.optIn("kotlin.contracts.ExperimentalContracts"); languageSettings.optIn("kotlin.ExperimentalStdlibApi") }
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("src/commonMain"))
            kotlin.srcDir("stubs")
            dependencies {
                api("org.jetbrains.kotlinx:atomicfu:0.23.2")
            }
        }
        jvmMain {
            kotlin.srcDir(layout.buildDirectory.dir("src/jbMain"))
            kotlin.srcDir(layout.buildDirectory.dir("src/jsNativeMain")) // jedyny actual: floatFromBits
        }
    }
}

tasks.configureEach { if (name.startsWith("compile") && name.contains("Kotlin")) dependsOn(unpack) }
tasks.configureEach { if (name.endsWith("SourcesJar")) dependsOn(unpack) }
