package com.encodeering.conflate.experimental.api

import kotlin.coroutines.experimental.suspendCoroutine

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
interface Completable<out Scope, out V> {

    fun then (fail : Scope.(Throwable) -> Unit, ok : Scope.(V) -> Unit = {})

}

suspend fun <Scope, V> Completable<Scope, V>.await () {
    suspendCoroutine<V> { continuation ->
        then (
            { continuation.resumeWithException (it) },
            { continuation.resume              (it) }
        )
    }
}