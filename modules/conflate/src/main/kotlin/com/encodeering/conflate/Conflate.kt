package com.encodeering.conflate

import com.encodeering.conflate.api.Action
import com.encodeering.conflate.api.Dispatcher
import com.encodeering.conflate.api.Middleware
import com.encodeering.conflate.api.Reducer
import com.encodeering.conflate.api.Storage
import com.encodeering.conflate.api.await
import com.encodeering.conflate.co.CycleDispatcher
import com.encodeering.conflate.util.trylog
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
class Conflate<out State> (
           context    : CoroutineContext = EmptyCoroutineContext,
           initial    : State,
           reducer    : Reducer<State>,
    vararg middleware : Middleware<State>
) : Storage<State> {

    private val conflation = AtomicReference<State> (initial)
    private val subscriptions = ConcurrentHashMap<Int, () -> Unit> ()

    private val connection = connect (initial, * middleware, conflation (reducer), notification ())

    override val dispatcher : Dispatcher = CycleDispatcher (context) { connection.next (it) }

    override val state : State
        get () = conflation.get ()

    override fun subscribe                (listener : () -> Unit) : () -> Unit {
        val key = System.identityHashCode (listener)

                 subscriptions.putIfAbsent (key,  { trylog { listener () } })
        return { subscriptions.remove      (key); }
    }

    private fun connect (initial : State, vararg middleware : Middleware<State>) : Middleware.Connection<State> {
        val last = Stop (initial)

        return middleware.foldRight (last as Middleware.Connection<State>) { middleware, connection ->
            object : Middleware.Connection<State> {

                override val state : State
                    get () = conflation.get()

                suspend override fun initial (action : Action) {
                    dispatcher.dispatch      (action).await ()
                }

                suspend override fun next    (action : Action) {
                    middleware.dispatch      (action, connection)
                }

            }
        }
    }

    private fun conflation (reducer : Reducer<State>) = Codeblock<State> { action, state ->
                            reducer.reduce                                (action, state).let {
                                conflation.compareAndSet (state, it).let {
                                    if (!it) { /* warning, interleaving reductions */
                                    }
                                }
                            }
    }

    private fun notification () = Codeblock<State> { _, _ -> subscriptions.values.forEach { it () } }

    private class Stop<out State> (override val state : State) : Middleware.Connection<State> {

        suspend override fun initial (action : Action) {}

        suspend override fun next    (action : Action) {}

    }

}
