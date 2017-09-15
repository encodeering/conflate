package com.encodeering.conflate.experimental.epic

import com.encodeering.conflate.experimental.api.Action
import io.reactivex.Observable

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
interface Story<State> {

    data class Aspect<out A : Action, out State> (
        val action : A,
        val state  : State
    )

    sealed class Happening (open     val action : Action) {

        data class Next    (override val action : Action) : Happening (action)
        data class Initial (override val action : Action) : Happening (action)

    }

    val endless : Boolean

    fun embellish (aspects : Aspects<Action, State>) : Happenings

}

typealias Aspects<A, State> = Observable<Story.Aspect<A, State>>

typealias Happenings = Observable<Story.Happening>