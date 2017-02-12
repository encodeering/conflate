package com.encodeering.conflate.util

/**
 * @author Michael Clausen - encodeering@gmail.com
 */

inline fun trylog (noinline log : (RuntimeException) -> Unit = { }, block : () -> Unit) = try {
    block()
} catch (e : RuntimeException) {
    log (e)
}
