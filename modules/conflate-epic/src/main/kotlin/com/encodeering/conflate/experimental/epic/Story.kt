package com.encodeering.conflate.experimental.epic

import com.encodeering.conflate.experimental.api.Action
import io.reactivex.Observable

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
interface Story<State> {

    data class Aspect<out State> (
        val action : Action,
        val state  : State
    )

    sealed class Happening (open     val action : Action) {

        data class Next    (override val action : Action) : Happening (action)
        data class Initial (override val action : Action) : Happening (action)

    }

    val endless : Boolean

    fun embellish (aspects : Aspects<State>) : Happenings

}

typealias Aspects<State> = Observable<Story.Aspect<State>>

typealias Happenings = Observable<Story.Happening>