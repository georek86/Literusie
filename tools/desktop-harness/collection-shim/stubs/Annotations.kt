@file:Suppress("unused")

package androidx.annotation

// Minimalne odpowiedniki adnotacji androidx.annotation używanych przez źródła androidx.collection.
@Retention(AnnotationRetention.BINARY)
annotation class IntRange(val from: Long = Long.MIN_VALUE, val to: Long = Long.MAX_VALUE)

@Retention(AnnotationRetention.BINARY)
annotation class RestrictTo(vararg val value: Scope) {
    enum class Scope { LIBRARY, LIBRARY_GROUP, LIBRARY_GROUP_PREFIX, GROUP_ID, TESTS, SUBCLASSES }
}
