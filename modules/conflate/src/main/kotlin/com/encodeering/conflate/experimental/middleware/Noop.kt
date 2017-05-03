package com.encodeering.conflate.experimental.middleware

import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.api.Middleware

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
class Noop<in State> : Middleware<State> {

    override fun interceptor (connection : Middleware.Connection<State>) : Middleware.Interceptor {
        return object : Middleware.Interceptor {

            suspend override fun dispatch (action : Action) = connection.next (action)

        }
    }

}
