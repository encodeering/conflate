package com.encodeering.conflate

import com.encodeering.conflate.api.Action
import com.encodeering.conflate.api.Middleware

internal class Codeblock<in State> (private val block : (Action, State) -> Unit) : Middleware<State> {

    override fun interceptor(connection : Middleware.Connection<State>) : Middleware.Interceptor {
        return object : Middleware.Interceptor {

            suspend override fun dispatch (action : Action) {
                connection.apply {
                    block   (action, connection.state)
                    next    (action)
                }
            }

        }
    }

}