package com.encodeering.conflate.experimental.test.fixture

import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.api.Reducer

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
object Reducers {

    fun accumulator (spy : (Action, Int) -> Unit = { _, _ -> Unit }) =
        object : Reducer<Int> {

            override fun reduce (action : Action, state : Int) : Int {
                spy             (action, state)

                return when     (action) {
                    is Sub ->    action.number - state
                    is Add ->    action.number + state
                    else   ->                    state
                }
            }

        }

}