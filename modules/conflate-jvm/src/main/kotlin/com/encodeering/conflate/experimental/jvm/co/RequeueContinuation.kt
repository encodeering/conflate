package com.encodeering.conflate.experimental.jvm.co

import kotlin.coroutines.experimental.Continuation

/**
 * A runtime exception to indicate a serious problem with the platform-coroutine interaction.
 *
 * @property message specifies an error message
 * @author Michael Clausen - encodeering@gmail.com
 */
data class RequeueException (override val message : String) : RuntimeException (message)

/**
 * A requeue continuation tries to resume the continuation on the current [thread][Thread], but would delay execution
 * otherwise and requeue with respect to the target platform.
 *
 * @property continuation specifies the intercepted continuation
 * @param T specifies the coroutine's value-type.
 * @author Michael Clausen - encodeering@gmail.com
 */
abstract class RequeueContinuation<in T> (private val continuation : Continuation<T>) : Continuation<T> {

    /**
     * Uses the context of the continuation.
     */
    override val context = continuation.context

    /**
     * Determines if the continuation can be called immediately.
     */
    abstract fun immediately () : Boolean

    /**
     * Defines a requeue strategy for the target platform.
     */
    abstract fun requeue (call : () -> Unit) : Boolean

    /**
     * Resumes this coroutine on the correct platform thread.
     */
    override fun resume (value : T) {
        post          { continuation.resume (value) }.let {
                  done ->
            if (! done) continuation.resumeWithException (RequeueException ("couldn't requeue '$value' message on the looper"))
        }
    }

    /**
     * Resumes this coroutine on the correct platform thread.
     */
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