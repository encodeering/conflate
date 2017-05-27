package com.encodeering.conflate.experimental.co

import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.api.Dispatcher
import kotlin.coroutines.experimental.CoroutineContext

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
class CycleDispatcher (private val context : CoroutineContext, private val f : suspend (Action) -> Unit) : Dispatcher {

    override fun dispatch (action : Action) = Cycle.co (context, this) { f (action) }

}