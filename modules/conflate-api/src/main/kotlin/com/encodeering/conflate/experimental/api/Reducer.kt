package com.encodeering.conflate.experimental.api

interface Reducer<State> {

    fun reduce (action : Action, state : State) : State

}