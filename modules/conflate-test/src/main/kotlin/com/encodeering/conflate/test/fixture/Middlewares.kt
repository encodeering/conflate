package com.encodeering.conflate.test.fixture

import com.encodeering.conflate.api.Action
import com.encodeering.conflate.api.Middleware
import com.encodeering.conflate.api.Storage

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
object Middlewares {

    fun connection (initial : (Action) -> Unit = { }, next : (Action) -> Unit = { }) = object : Middleware.Connection {

        suspend override fun initial (action : Action) {
            initial                  (action)
        }

        suspend override fun next    (action : Action) {
            next                     (action)
        }

    }

    fun <T> middleware (before : (Action, Storage<T>, Middleware.Connection) -> Unit = { _, _, _ -> Unit },
                        after  : (Action, Storage<T>, Middleware.Connection) -> Unit = { _, _, _ -> Unit }) =
        object : Middleware<T> {

            suspend override fun dispatch (action : Action, storage : Storage<T>, connection : Middleware.Connection) {
                before                    (action,          storage,              connection)
                connection.next           (action)
                after                     (action,          storage,              connection)
            }

        }

}

