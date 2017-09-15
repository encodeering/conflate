package com.encodeering.conflate.experimental.epic.rx

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.coroutines.experimental.startCoroutine
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
suspend fun <T> Observable<T>.await (f : (T) -> Unit = {}) {
    suspendCoroutine<Unit> { continuation ->
        subscribe (
            { f                                (it)   },
            { continuation.resumeWithException (it)   },
            { continuation.resume              (Unit) }
        )
    }
}

fun <T, R> Observable<T>.async (f : suspend (T) -> R, context : CoroutineContext = EmptyCoroutineContext) : Observable<R> {
    val subject = PublishSubject.create<R> ()

    subscribe ({
        if (subject.run { hasComplete () || hasThrowable ()})
            return@subscribe

        f.startCoroutine (it, object : Continuation<R> {

            override val context = context

            override fun resume (value : R) {
                subject.onNext  (value)
            }

            override fun resumeWithException (exception : Throwable) {
                subject.onError              (exception)
            }

        })
    }, subject::onError, { subject.onComplete () })

    return subject
}

fun <T> Iterable<Observable<T>>.combine (delay : Boolean = true) : Observable<T> =
    if (delay) Observable.mergeDelayError (this)
    else       Observable.merge (this)
