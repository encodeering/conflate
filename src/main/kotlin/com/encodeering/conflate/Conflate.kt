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

    private val connection = pipeline (reducer, * middleware).foldRight (Stop as Middleware.Connection) {
        middleware, connection ->
            object : Middleware.Connection {

                suspend override fun begin   (action : Action) {
                    dispatcher.dispatch      (action).await ()
                }

                suspend override fun proceed (action : Action) {
                    middleware.dispatch      (action, this@Conflate, connection)
                }

            }
    }

    override val dispatcher : Dispatcher = CycleDispatcher (context) { connection.proceed (it) }

    override val state : State
        get () = conflation.get ()

    override fun subscribe                (listener : () -> Unit) : () -> Unit {
        val key = System.identityHashCode (listener)

                 subscriptions.putIfAbsent (key,  { trylog { listener () } })
        return { subscriptions.remove      (key); }
    }

    private fun pipeline (reducer : Reducer<State>, vararg middleware : Middleware<State>) =
            middleware.toList () +
                    Delegate {          action, state ->
                        reducer.reduce (action, state).let {
                            conflation.compareAndSet(state, it).let {
                                if (!it) { /* warning, interleaving reductions */
                                }
                            }
                        }
                    } +
                    Delegate { _, _  -> subscriptions.values.forEach { it () } }

    private class Delegate<in State> (private val block : (Action, State) -> Unit) : Middleware<State> {

        suspend override fun dispatch (action : Action, storage : Storage<State>, connection : Middleware.Connection) {
            connection.apply {
                block   (action, storage.state)
                proceed (action)
            }
        }

    }
    private object Stop : Middleware.Connection {

        suspend override fun begin   (action : Action) {}

        suspend override fun proceed (action : Action) {}

    }

}
