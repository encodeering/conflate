package com.encodeering.conflate.experimental.jfx.co

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
class FxATTest : Spek ({

    describe ("FxATContinuation") {

        /* jfx application thread can't be tested easily */

    }

    describe ("FxATInterceptor") {

        fun <T> continuation () = mock<Continuation<T>> ()

        it ("should wrap the continuation") {
            val     wrap = FxAT.interceptContinuation (continuation<Any> ())
            expect (wrap).to.satisfy { it is FxATContinuation }
        }

        it ("should use the interception key") {
            expect (FxAT.key).to.equal (ContinuationInterceptor.Key)
        }

    }

})
