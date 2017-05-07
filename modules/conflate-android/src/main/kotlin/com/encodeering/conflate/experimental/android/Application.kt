package com.encodeering.conflate.experimental.android

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import com.encodeering.conflate.experimental.Conflate
import com.encodeering.conflate.experimental.android.co.Looper
import com.encodeering.conflate.experimental.api.Middleware
import com.encodeering.conflate.experimental.api.Reducer
import com.encodeering.conflate.experimental.logging.Logging
import com.encodeering.conflate.experimental.middleware.Noop
import kotlin.coroutines.experimental.CoroutineContext

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
fun <State> Application.conflate (
           initial    : State,
           reducer    : Reducer<State>,
    vararg middleware : Middleware<State>
) : Conflate<State> = Conflate (initial, reducer, Looper, * middleware)

fun <State> Application.conflate (
           initial    : State,
           reducer    : Reducer<State>,
           context    : CoroutineContext,
    vararg middleware : Middleware<State>
) : Conflate<State> = Conflate (initial, reducer, Looper + context, * middleware)

fun <State> Application.logging (tag : String = Conflate::class.java.name) : Middleware<State> =
    if (debuggable ())
        Logging<State> (
            log = {
                text, action -> Log.d (tag, "$text $action")
            }
        )
    else Noop<State> ()

fun Application.debuggable () = 0 != (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)
