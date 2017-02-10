package com.encodeering.conflate.middleware

import com.encodeering.conflate.api.Action
import com.encodeering.conflate.api.Middleware
import com.encodeering.conflate.api.Storage
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

    suspend override fun dispatch (action : Action, storage : Storage<State>, connection : Middleware.Connection) {
        if (before ()) debug (">>", action)

        try {
            connection.proceed (action)

            if (after ())     debug ("--", action)
        } catch (e : Exception) {
            if (exception ()) debug ("!!", action)

            throw e
        }
    }

    private fun debug (prefix : CharSequence, action : Action) = log ("$prefix {}", action)

    companion object {

        private val logger : Logger by lazy { LoggerFactory.getLogger (Logging::class.java) }

    }

}