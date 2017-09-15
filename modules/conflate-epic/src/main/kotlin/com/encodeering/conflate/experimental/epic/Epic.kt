package com.encodeering.conflate.experimental.epic

import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.api.Middleware
import com.encodeering.conflate.experimental.epic.Story.Aspect
import com.encodeering.conflate.experimental.epic.Story.Happening
import com.encodeering.conflate.experimental.epic.rx.async
import com.encodeering.conflate.experimental.epic.rx.await
import com.encodeering.conflate.experimental.epic.rx.combine
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
class Epic<State> (
   private val    context : CoroutineContext,
           vararg stories : Story<State>
) : Middleware<State> {

    constructor (vararg stories : Story<State>) : this (EmptyCoroutineContext, * stories)

    val daemons by lazy { stories.filter    { it.endless } }
    val visuals by lazy { stories.filterNot { it.endless } }

    override fun interceptor (connection : Middleware.Connection<State>) : Middleware.Interceptor {
        return object : Middleware.Interceptor {

            val daemons by lazy { this@Epic.daemons.map (this::raconteur) }
            val visuals get ()  = this@Epic.visuals.map (this::raconteur)

            fun raconteur (story : Story<State>) : Raconteur<State> {
                val forward : suspend (Happening) -> Happening = {
                    when                                           (it) {
                        is Happening.Next    -> connection.next    (it.action)
                        is Happening.Initial -> connection.initial (it.action)
                    }

                    it
                }

                val               aspects = PublishSubject.create<Aspect<Action, State>> ()!!
                return Raconteur (aspects, story.embellish (aspects).async (forward, context))
            }

            suspend override fun dispatch (action : Action) {
                val aspect = Aspect       (action, connection.state)

                daemons.run                                                  { tell (aspect, finish = false) }
                visuals.run { map { it.happenings }.combine ().doOnSubscribe { tell (aspect, finish =  true) } }.await ()
            }

        }
    }

    private fun Iterable<Raconteur<State>>.tell (aspect : Aspect<Action, State>, finish : Boolean) {
        forEach {
            try {
                it.tell (aspect)
            } catch      (e : Exception) {
                it.abort (e)
            } finally {
                if (finish) it.finish ()
            }
        }
    }

    private class Raconteur<in State> (private val aspects : Subject<Aspect<Action, State>>, val happenings : Observable<Happening>) {

        fun tell (aspect : Aspect<Action, State>) = aspects.onNext (aspect)

        fun abort (e : Exception)         = aspects.onError (e)

        fun finish ()                     = aspects.onComplete ()

    }

}
