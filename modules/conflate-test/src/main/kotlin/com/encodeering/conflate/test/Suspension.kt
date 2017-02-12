package com.encodeering.conflate.test

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.coroutines.experimental.startCoroutine

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
fun co (context : CoroutineContext = EmptyCoroutineContext, timeout : Long = Long.MAX_VALUE, block : suspend () -> Unit) {
    val error = AtomicReference<Throwable> ()

    val latch = CountDownLatch (1)

    block.startCoroutine (completion = object : Continuation<Unit> {

        override val context = context

        override fun resume (value : Unit) {
            latch.countDown ()
        }

        override fun resumeWithException (exception : Throwable) {
            error.compareAndSet (null, exception)
            latch.countDown ()
        }

    })

    if (! latch.await (timeout, TimeUnit.MILLISECONDS))
          error.compareAndSet (null, AssertionError ("Timeout of $timeout ms exceeded"))

    error.get ()?.let { throw it }
}