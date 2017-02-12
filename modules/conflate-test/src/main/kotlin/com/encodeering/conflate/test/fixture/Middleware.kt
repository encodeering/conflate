package com.encodeering.conflate.test.fixture

import com.encodeering.conflate.api.Action
import com.encodeering.conflate.api.Middleware
import com.encodeering.conflate.api.Storage

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
object Middleware {

    fun connection (begin : (Action) -> Unit = { }, proceed : (Action) -> Unit = { }) = object : Middleware.Connection {

        suspend override fun begin (action : Action) {
            begin (action)
        }

        suspend override fun proceed (action : Action) {
            proceed (action)
        }

    }

    fun <T> middleware (before : (Action, Storage<T>, Middleware.Connection) -> Unit = { _, _, _ -> Unit },
                        after  : (Action, Storage<T>, Middleware.Connection) -> Unit = { _, _, _ -> Unit }) =
        object : Middleware<T> {

            suspend override fun dispatch (action : Action, storage : Storage<T>, connection : Middleware.Connection) {
                before                    (action,          storage,              connection)
                connection.proceed        (action)
                after                     (action,          storage,              connection)
            }

        }

}

