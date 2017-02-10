package com.encodeering.conflate.fixture

import com.encodeering.conflate.api.Action
import com.encodeering.conflate.api.Middleware

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

}

