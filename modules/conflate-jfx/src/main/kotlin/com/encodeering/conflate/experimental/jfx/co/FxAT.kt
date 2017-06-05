package com.encodeering.conflate.experimental.jfx.co

import com.encodeering.conflate.experimental.co.RequeueContinuation
import javafx.application.Platform
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.ContinuationInterceptor

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
class FxATContinuation<in T> (continuation : Continuation<T>) : RequeueContinuation<T> (continuation) {

    override fun immediately () : Boolean = Platform.isFxApplicationThread ()

    override fun requeue  (call : () -> Unit) : Boolean {
        Platform.runLater (call)
        return true
    }

}

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
object FxAT : AbstractCoroutineContextElement (ContinuationInterceptor), ContinuationInterceptor {

    override fun <T> interceptContinuation (continuation : Continuation<T>) = FxATContinuation (continuation)

}
