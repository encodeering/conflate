package com.encodeering.conflate.experimental.api

import kotlin.coroutines.experimental.suspendCoroutine

/**
 * A Completable wraps a computational process, whose final state can be inspected using callbacks.
 *
 * The invocation order for callbacks is unspecified and may not stay in the perceived order.
 *
 * @param Scope specifies a convenience runtime scope
 * @param V specifies a convenience success value
 * @author Michael Clausen - encodeering@gmail.com
 */
interface Completable<out Scope, out V> {

    /**
     * Registers callbacks at this completable that get notified, as soon as the computation reaches a final state.
     *
     * A callback can be registered at any time, even if the process has already finished.
     * Lately registered callback will be invoked immediately with the known outcome of the completable.
     *
     * @param fail specifies a mandatory callback to observe the error state
     * @param ok specifies an optional callback to observer the success state; a noop by default
     */
    fun then (fail : Scope.(Throwable) -> Unit, ok : Scope.(V) -> Unit = {})

}

/**
 * Suspends this coroutine until the final state of this completable has been reached, which will then resume this coroutine in
 * either case.
 */
suspend fun <Scope, V> Completable<Scope, V>.await () {
    suspendCoroutine<V> { continuation ->
        then (
            { continuation.resumeWithException (it) },
            { continuation.resume              (it) }
        )
    }
}