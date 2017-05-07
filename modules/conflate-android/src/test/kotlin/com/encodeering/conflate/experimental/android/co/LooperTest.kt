package com.encodeering.conflate.experimental.android.co

import com.encodeering.conflate.experimental.test.mock
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.ContinuationInterceptor

/**
 * @author Michael Clausen - encodeering@gmail.com
 */

@RunWith (JUnitPlatform::class)
class LooperTest : Spek ({

    describe ("LooperContinuation") {

        /* android looper can't be tested easily */

    }

    describe ("LooperInterceptor") {

        fun <T> continuation () = mock<Continuation<T>> ()

        it ("should wrap the continuation") {
            val     wrap = Looper.interceptContinuation (continuation<Any> ())
            expect (wrap).to.satisfy { it is LooperContinuation }
        }

        it ("should use the interception key") {
            expect (Looper.key).to.equal (ContinuationInterceptor.Key)
        }

    }

})
