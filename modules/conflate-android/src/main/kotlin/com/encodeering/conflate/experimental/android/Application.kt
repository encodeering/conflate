package com.encodeering.conflate.experimental.android

import android.app.Application
import com.encodeering.conflate.experimental.Conflate
import com.encodeering.conflate.experimental.android.co.Looper
import com.encodeering.conflate.experimental.api.Middleware
import com.encodeering.conflate.experimental.api.Reducer
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
