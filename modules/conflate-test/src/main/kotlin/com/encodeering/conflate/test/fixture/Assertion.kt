package com.encodeering.conflate.test.fixture

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
inline fun <reified T : Throwable> throws (block : () -> Unit) {
    var ex : Throwable? = null
    var thrown  = false
    var matches = false

    try {
        block ()
    } catch                           (e : Throwable) {
        ex =                           e
        matches = T::class.isInstance (e)
        thrown  = true

    } finally {
        if (! matches && ex != null) throw AssertionError ("block should have thrown a ${T::class.simpleName}, but threw a ${ex.javaClass.simpleName}")
        if (! thrown) throw AssertionError ("block should have thrown a ${T::class.simpleName}")
    }
}