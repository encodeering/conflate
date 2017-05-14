package com.encodeering.conflate.experimental.api

/**
 * A reducer conflates the payload of an action with the given state into a state object, and shall not
 * perform any kind of side-effect, that influences the outcome of the returned state object.
 *
 * An implementation must guarantee, that he following conditions are met:
 *
 * - Invocations with same arguments lead to same results, regardless of the invocation or execution time.
 *    ```
 *    assert reduce (A0, S0) == reduce (A0, S0)
 *    ```
 * - Modifications to any state do not affect other states, regardless of the invocation or execution time.
 *    ```
 *    assert reduce (A0, S0) != reduce (A0, S0).apply { this.p += 1 }
 *    assert reduce (A0, S0) != reduce (A0, S0.apply  { this.p += 1 })
 *    ```
 *
 * Any of the following items may cause a side-effect, that contravenes one of the stated condition:
 *
 * - I/O
 * - Service-Computation
 * - Date/Time
 * - Seeding
 * - ...
 *
 * Any part of the previous state may be freely recycled, as long as the related parts to do not violate the contract or
 * prevent any form of memory cleanup.
 *
 * ### Official documentation:
 *
 * - Actions describe the fact that something happened, but don't specify how the application's state changes in response.
 *  This is the job of reducers. [http://redux.js.org/docs/basics/Reducers.html]
 *
 * - Given the same arguments, it should calculate the next state and return it. No surprises. No side effects. No API calls. No mutations.
 *  Just a calculation. [http://redux.js.org/docs/basics/Reducers.html]
 *
 * ### Sample
 *
 * ```
 * data class Sub (val number : Int) : Action
 * data class Add (val number : Int) : Action
 *
 * object : Reducer<Int> {
 *
 *     override fun reduce (action : Action, state : Int) : Int {
 *         return when     (action) {
 *             is Sub ->    action.number - state
 *             is Add ->    action.number + state
 *             else   ->                    state
 *         }
 *     }
 *
 * }
 *  ```
 *
 * @param State defines the type of the state container
 * @author Michael Clausen - encodeering@gmail.com
 */
interface Reducer<State> {

    /**
     * Defines the actual conflation strategy.
     *
     * @param action specifies an action, whose payload shall be merged with the application's state
     * @param state specifies a state container
     * @return a new state container
     */
    fun reduce (action : Action, state : State) : State

}