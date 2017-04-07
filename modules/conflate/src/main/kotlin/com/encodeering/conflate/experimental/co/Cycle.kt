package com.encodeering.conflate.experimental.co

import com.encodeering.conflate.experimental.api.Completable
import com.encodeering.conflate.experimental.util.trylog
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.startCoroutine

class Cycle<out Scope, V> (override val context : CoroutineContext, val scope : Scope) : Completable<Scope, V>, Continuation<V> {

    private var offer : V? = null
    private var error : Throwable? = null

    private val state = AtomicInteger (2)

    private val failure = ConcurrentLinkedQueue<Scope.(Throwable) -> Unit> ()
    private val success = ConcurrentLinkedQueue<Scope.(V)         -> Unit> ()

    override fun then (fail : Scope.(Throwable) -> Unit, ok : Scope.(V) -> Unit) {
        failure.add (fail)
        success.add (ok)

        inform ()
    }

    override fun resume (value : V) {
        if (state.compareAndSet (2, 1)) { // memory read/write barrier
            offer = value
            state.set (0) // memory write barrier
            inform ()
        }
    }

    override fun resumeWithException (exception : Throwable) {
        if (state.compareAndSet (2, 1)) { // memory read/write barrier
            error = exception
            state.set (0) // memory write barrier
            inform ()
        }
    }

    private fun inform () {
        if (state.get () == 0) // memory read barrier
            error?.let { e -> success.clear (); failure.forPoll { trylog { it.invoke (scope, e) } } } ?:
            offer?.let { v -> failure.clear (); success.forPoll { trylog { it.invoke (scope, v) } } }
    }

    companion object {

        fun <Scope> co (context : CoroutineContext, scope : Scope, block : suspend () -> Unit)  : Completable<Scope, Unit> {
            val                                completable = Cycle<Scope, Unit> (context, scope)
            block.startCoroutine (completion = completable)

            return completable
        }

    }

}

private fun <T> Queue<T>.forPoll (action : (T) -> Unit) {
    var         element : T? = poll ()
    while      (element != null) {
        action (element)
                element = poll ()
    }
}