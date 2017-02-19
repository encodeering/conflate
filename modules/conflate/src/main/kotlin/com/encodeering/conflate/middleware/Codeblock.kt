package com.encodeering.conflate

import com.encodeering.conflate.api.Action
import com.encodeering.conflate.api.Middleware

internal class Codeblock<in State> (private val block : (Action, State) -> Unit) : Middleware<State> {

    suspend override fun dispatch (action : Action, connection : Middleware.Connection<State>) {
        connection.apply {
            block   (action, connection.state)
            next    (action)
        }
    }

}