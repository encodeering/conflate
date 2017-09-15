package com.encodeering.conflate.experimental.epic

import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
fun <T> just (item : T) = Observable.just (item)

fun <T> callable (callable : () -> T) = Observable.fromCallable { callable () }

fun <T> delayed (item : T, delay : Long = 10) = just (item).delay (delay, TimeUnit.MILLISECONDS)

fun <T> delayed (vararg item : T, delay : Long = 10) = Observable.fromArray (* item).delay (delay, TimeUnit.MILLISECONDS)