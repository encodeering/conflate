package com.encodeering.conflate.experimental.middleware

import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.api.Middleware

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
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