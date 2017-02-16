package com.encodeering.conflate.test.fixture

import com.encodeering.conflate.api.Action
import com.encodeering.conflate.api.Middleware

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
object Middlewares {

    fun connection (initial : (Action) -> Unit = { }, next : (Action) -> Unit = { }) = object : Middleware.Connection<Unit> {

        override val state : Unit
            get () = Unit

        suspend override fun initial (action : Action) {
            initial                  (action)
        }

        suspend override fun next    (action : Action) {
            next                     (action)
        }

    }

    fun <T> middleware (before : (Action, Middleware.Connection<T>) -> Unit = { _, _ -> Unit },
                        after  : (Action, Middleware.Connection<T>) -> Unit = { _, _ -> Unit }) =
        object : Middleware<T> {

            suspend override fun dispatch (action : Action, connection : Middleware.Connection<T>) {
                before                    (action,          connection)
                connection.next           (action)
                after                     (action,          connection)
            }

        }

}

