package com.encodeering.conflate.logging

import com.encodeering.conflate.api.Action
import com.encodeering.conflate.api.Middleware
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
class Logging<in State> (
        val log       : (String, Action) -> Unit = Logging.logger::debug,
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

            private fun debug (prefix : CharSequence, action : Action) = log ("$prefix {}", action)

        }
    }

    companion object {

        private val logger : Logger by lazy { LoggerFactory.getLogger (Logging::class.java) }

    }

}