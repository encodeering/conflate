package com.encodeering.conflate.experimental.co

import kotlin.coroutines.experimental.Continuation

data class RequeueException (override val message : String) : RuntimeException (message)

abstract class RequeueContinuation<in T> (private val continuation : Continuation<T>) : Continuation<T> {

    override val context = continuation.context

    abstract fun immediately () : Boolean

    abstract fun requeue (call : () -> Unit) : Boolean

    override fun resume (value : T) {
        post          { continuation.resume (value) }.let {
                  done ->
            if (! done) continuation.resumeWithException (RequeueException ("couldn't requeue '$value' message on the looper"))
        }
    }

    override fun resumeWithException (exception : Throwable) {
        post          { continuation.resumeWithException (exception) }.let {
                  done ->
            if (! done) continuation.resumeWithException (RequeueException ("couldn't requeue '$exception' message on the looper"))
        }
    }

    private fun post (action : () -> Unit) : Boolean =
        if (immediately ()) {
            action ()
            true
        } else {
            requeue (action)
        }

}