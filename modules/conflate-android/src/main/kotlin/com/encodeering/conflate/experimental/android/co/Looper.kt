package com.encodeering.conflate.experimental.android.co

import android.os.Handler
import android.os.Looper
import com.encodeering.conflate.experimental.co.RequeueContinuation
import java.lang.Thread.currentThread
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.ContinuationInterceptor

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
class LooperContinuation<in T> (continuation : Continuation<T>) : RequeueContinuation<T> (continuation) {

    private  val                  handler = Handler (Looper.getMainLooper ())

    override fun immediately () = handler.looper.thread == currentThread ()

    override fun requeue (call : () -> Unit) = handler.post (call)

}

object Looper : AbstractCoroutineContextElement (ContinuationInterceptor), ContinuationInterceptor {

    override fun <T> interceptContinuation (continuation : Continuation<T>) = LooperContinuation (continuation)

}