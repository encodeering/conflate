package com.encodeering.conflate.api

interface Middleware<in State> {

    fun interceptor (connection : Connection<State>) : Interceptor

    interface Connection<out State> {

        val state : State

        suspend fun initial (action : Action)

        suspend fun next    (action : Action)

    }

    interface Interceptor {

        suspend fun dispatch (action : Action)

    }

}