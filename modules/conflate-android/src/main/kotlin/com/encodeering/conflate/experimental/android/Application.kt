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
 * Configures conflate for Android.
 *
 * @param initial specifies the initial state
 * @param reducer specifies the conflation strategy
 * @param middleware specifies a list of middleware components
 * @param State defines the type of the state container
 */
fun <State> Application.conflate (
           initial    : State,
           reducer    : Reducer<State>,
    vararg middleware : Middleware<State>
) : Conflate<State> = Conflate (initial, reducer, Looper, * middleware)

/**
 * Configures conflate for Android.
 *
 * @see conflate
 * @param initial specifies the initial state
 * @param reducer specifies the conflation strategy
 * @param context specifies the additional coroutine context
 * @param middleware specifies a list of middleware components
 * @param State defines the type of the state container
 */
fun <State> Application.conflate (
           initial    : State,
           reducer    : Reducer<State>,
           context    : CoroutineContext,
    vararg middleware : Middleware<State>
) : Conflate<State> = Conflate (initial, reducer, Looper + context, * middleware)

/**
 * Creates a logging middleware using the native capabilities of Android.
 *
 * A noop middleware will be returned, if the application is not [debuggable].
 */
fun <State> Application.logging (tag : String = Conflate::class.java.name) : Middleware<State> =
    if (debuggable ())
        Logging<State> (
            log = {
                text, action -> Log.d (tag, "$text $action")
            }
        )
    else Noop<State> ()

/**
 * Determines if the application has been compiled with debug settings
 */
fun Application.debuggable () = 0 != (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)
