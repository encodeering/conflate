package com.encodeering.conflate.api

interface Middleware<in State> {

    suspend fun dispatch (action : Action, connection : Connection<State>)

    interface Connection<out State> {

        val state : State

        suspend fun initial (action : Action)

        suspend fun next    (action : Action)

    }

}