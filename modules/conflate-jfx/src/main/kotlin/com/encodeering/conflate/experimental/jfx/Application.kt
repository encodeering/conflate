package com.encodeering.conflate.experimental.jfx

import com.encodeering.conflate.experimental.Conflate
import com.encodeering.conflate.experimental.api.Middleware
import com.encodeering.conflate.experimental.api.Reducer
import com.encodeering.conflate.experimental.jfx.co.FxAT
import com.encodeering.conflate.experimental.logging.Logging
import javafx.application.Application
import org.slf4j.LoggerFactory
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Configures conflate for JFx.
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
) : Conflate<State> = Conflate (initial, reducer, FxAT, * middleware)

/**
 * Configures conflate for JFx.
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
) : Conflate<State> = Conflate (initial, reducer, FxAT + context, * middleware)

/**
 * Creates a logging middleware using slf4j.
 */
fun <State> Application.logging (tag : String = Conflate::class.java.name) : Middleware<State> {
    val                                 logger by lazy { LoggerFactory.getLogger (tag) }
    return Logging ({ prefix, action -> logger.debug ("{} {}", prefix, action) })
}
