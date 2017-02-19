package com.encodeering.conflate.api

interface Middleware<in State> {

    suspend fun dispatch (action : Action, storage : Storage<State>, connection : Connection)

    interface Connection {

        suspend fun initial (action : Action)

        suspend fun next    (action : Action)

    }

}