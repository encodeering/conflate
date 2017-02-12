package com.encodeering.conflate.test.fixture

import com.encodeering.conflate.api.Action
import com.encodeering.conflate.api.Reducer

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
object Reducers {

    fun accumulator (spy : (Action, Int) -> Unit = { _, _ -> Unit }) =
        object : Reducer<Int> {

            override fun reduce (action : Action, state : Int) : Int {
                spy             (action, state)

                return when     (action) {
                    is Add ->    action.number + state
                    else   ->                    state
                }
            }

        }

}