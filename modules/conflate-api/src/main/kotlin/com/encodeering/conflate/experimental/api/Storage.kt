package com.encodeering.conflate.experimental.api

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
interface Storage<out State> {

    val state : State

    val dispatcher : Dispatcher

    fun subscribe (listener : () -> Unit) : Runnable

}

fun <State>       Storage<State>.unbox (r : State.(Dispatcher) -> Unit) = state.r (dispatcher)
fun <State, Part> Storage<State>.unbox (t : State.() -> Part, r : Part.(Dispatcher) -> Unit) = state.t ().r (dispatcher)