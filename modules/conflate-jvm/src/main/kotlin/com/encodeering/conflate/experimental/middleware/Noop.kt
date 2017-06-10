package com.encodeering.conflate.experimental.middleware

import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.api.Middleware

/**
 * A no-operation middleware that simply delegates any action to the next middleware component.
 *
 * Middleware is `thread-safe` and supports `linearization`.
 *
 * @param State defines the type of the state container
 * @author Michael Clausen - encodeering@gmail.com
 */
class Noop<in State> : Middleware<State> {

    override fun interceptor (connection : Middleware.Connection<State>) : Middleware.Interceptor {
        return object : Middleware.Interceptor {

            suspend override fun dispatch (action : Action) = connection.next (action)

        }
    }

}
