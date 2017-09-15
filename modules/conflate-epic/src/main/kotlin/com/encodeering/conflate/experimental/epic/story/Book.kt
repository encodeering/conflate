package com.encodeering.conflate.experimental.epic.story

import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.epic.Aspects
import com.encodeering.conflate.experimental.epic.Happenings
import com.encodeering.conflate.experimental.epic.Story

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
object Book {


    fun <State> anecdote                     (embellish : (Aspects<Action, State>) -> Happenings = flip ()) =
           Book.anecdote (Action::class.java, embellish)

    fun <A : Action, State> anecdote (type : Class<A>, embellish : (Aspects<A, State>) -> Happenings) =
        object : Story<State> {

            override val endless = false

            override fun embellish (aspects : Aspects<Action, State>) = embellish (Book.filter (aspects, type))

        }

    fun <State> roman                     (embellish : (Aspects<Action, State>) -> Happenings = flip ()) =
           Book.roman (Action::class.java, embellish)

    fun <A : Action, State> roman (type : Class<A>, embellish : (Aspects<A, State>) -> Happenings) =
        object : Story<State> {

            override val endless = true

            override fun embellish (aspects : Aspects<Action, State>) = embellish (Book.filter (aspects, type))

        }

    @Suppress("UNCHECKED_CAST")
    fun <A : Action, State> filter (aspects : Aspects<Action, State>, type : Class<A>) =
                                    aspects.filter { type.isInstance (it.action)  }
                                           .map    { it as Story.Aspect<A, State> }!!

    fun <A : Action, State> flip () : (Aspects<A, State>) -> Happenings = { it.map { Story.Happening.Next (it.action) } }

}