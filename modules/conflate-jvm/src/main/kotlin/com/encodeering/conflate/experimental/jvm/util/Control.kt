package com.encodeering.conflate.experimental.jvm.util

/**
 * Guards a call with a try-catch on [runtime-exception][java.lang.RuntimeException] and logs all errors using the provided [logger][log].
 */
inline fun trylog (noinline log : (RuntimeException) -> Unit = { }, block : () -> Unit) = try {
    block()
} catch (e : RuntimeException) {
    log (e)
}
