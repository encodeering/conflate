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

    private val connection = connect (* middleware, conflation (reducer::reduce, conflation::compareAndSet), notification (subscriptions::values))

    override val dispatcher : Dispatcher = CycleDispatcher (context) { connection.next (it) }

    override val state : State
        get () = conflation.get ()

    override fun subscribe                (listener : () -> Unit) : () -> Unit {
        val key = System.identityHashCode (listener)

                 subscriptions.putIfAbsent (key,  { trylog { listener () } })
        return { subscriptions.remove      (key); }
    }

    private fun connect (vararg middleware : Middleware<State>) : Middleware.Connection<State> {
        val              dispatch : suspend (Action) -> Unit =
            { dispatcher.dispatch (it).await () }

        return middleware.foldRight (Stop (conflation::get) as Middleware.Connection<State>) { mw, con -> Next (conflation::get, dispatch, mw, con) }
    }

    private fun conflation (conflate : (Action, State) -> State, persist : (State, State) -> Boolean) = Codeblock<State> { action, state ->
                            conflate                                                                                      (action, state).let {
                                persist (state, it).let {
                                    if (! it) { /* warning, interleaving reductions */
                                        throw IllegalStateException ("Could not persist the state of $action due an interleaving reduction step")
                                    }
                                }
                            }
    }

    private fun notification (subscriptions : () -> Collection<() -> Unit>) = Codeblock<State> { _, _ -> subscriptions ().forEach { it () } }

    private class Stop<out State> (private var resolve : () -> State) : Middleware.Connection<State> {

        override val state : State
            get() = resolve ()

        suspend override fun initial (action : Action) {}

        suspend override fun next    (action : Action) {}

    }

    private class Next<out State> (private var resolve : () -> State, private val dispatch : suspend (Action) -> Unit, private val middleware : Middleware<State>, private val next : Middleware.Connection<State>) : Middleware.Connection<State> {

        val interceptor = middleware.interceptor (next)

        override val state : State
            get () = resolve ()

        suspend override fun initial (action : Action) {
            dispatch                 (action)
        }

        suspend override fun next    (action : Action) {
            interceptor.dispatch     (action)
        }

    }

}
