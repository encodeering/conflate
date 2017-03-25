package com.encodeering.conflate.experimental.epic.story

import com.encodeering.conflate.experimental.epic.Aspects
import com.encodeering.conflate.experimental.epic.Happenings
import com.encodeering.conflate.experimental.epic.Story

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
object Book {

    fun <State> anecdote (embellish : (Aspects<State>) -> Happenings = flip<State> ()) =
        object : Story<State> {

            override val endless = false

            override fun embellish (aspects : Aspects<State>) = embellish (aspects)

        }

    fun <State> roman    (embellish : (Aspects<State>) -> Happenings = flip<State> ()) =
        object : Story<State> {

            override val endless = true

            override fun embellish (aspects : Aspects<State>) = embellish (aspects)

        }

    private fun <State> flip () : (Aspects<State>) -> Happenings =
        { it.map { Story.Happening.Next (it.action) } }

}