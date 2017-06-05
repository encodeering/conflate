package com.encodeering.conflate.experimental.logging

import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.api.Middleware

/**
 * A logging middleware for debugging purposes.
 *
 * Middleware is `thread-safe`, if the underlying callbacks are thread-safe, and supports `linearization`.
 *
 * @property log specifies the logging facility
 * @property before specifies if a log statement shall be made before any call to the next middleware, default is `true`
 * @property after specifies if a log statement shall be made after a call to the next middleware, default if `false`
 * @property exception specifies if a log statement shall be made if an exception occurred, default is `true`
 * @param State defines the type of the state container
 * @author Michael Clausen - encodeering@gmail.com
 */
class Logging<in State> (
        val log       : (String, Action) -> Unit,
        val before    : () -> Boolean = { true  },
        val after     : () -> Boolean = { false },
        val exception : () -> Boolean = { true  }
) : Middleware<State> {

    override fun interceptor (connection : Middleware.Connection<State>) : Middleware.Interceptor {
        return object : Middleware.Interceptor {

            suspend override fun dispatch (action : Action) {
                if (before ()) debug (">>", action)

                try {
                    connection.next (action)

                    if (after ())     debug ("--", action)
                } catch (e : Exception) {
                    if (exception ()) debug ("!!", action)

                    throw e
                }
            }

            private fun debug (prefix : String, action : Action) = log (prefix, action)

        }
    }

}