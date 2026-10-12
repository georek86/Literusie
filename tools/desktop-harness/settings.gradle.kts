// Pomocniczy projekt JVM/Compose Desktop. Kompiluje WSPÓLNY kod aplikacji (domena + interfejs Compose)
// i uruchamia testy jednostkowe oraz testy UI bez Androida i bez emulatora.
// To NIE jest build aplikacji na Android TV – ten jest w katalogu głównym (moduł :app).
pluginManagement {
    repositories { gradlePluginPortal(); mavenCentral() }
}
dependencyResolutionManagement {
    repositories { mavenCentral() }
}
rootProject.name = "literusie-harness"

// Zastępnik androidx.collection (patrz collection-shim/build.gradle.kts).
includeBuild("collection-shim") {
    dependencySubstitution { substitute(module("androidx.collection:collection")).using(project(":")) }
}
