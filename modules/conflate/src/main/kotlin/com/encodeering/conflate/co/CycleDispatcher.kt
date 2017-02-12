package com.encodeering.conflate.co

import com.encodeering.conflate.api.Action
import com.encodeering.conflate.api.Completable
import com.encodeering.conflate.api.Dispatcher
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.startCoroutine

/**
 * @author Michael Clausen - encodeering@gmail.com
 */

class CycleDispatcher (private val context : CoroutineContext, private val f : suspend (Action) -> Unit) : Dispatcher {

    private fun co (block : suspend () -> Unit)  : Completable<Dispatcher, Unit> {
        val                                completable = Cycle<Dispatcher, Unit> (context, this)
        block.startCoroutine (completion = completable)

        return completable
    }

    override fun dispatch (action : Action) = co { f (action) }

}