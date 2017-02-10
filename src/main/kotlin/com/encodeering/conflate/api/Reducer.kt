package com.encodeering.conflate.api

interface Reducer<State> {

    fun reduce (action : Action, state : State) : State

}