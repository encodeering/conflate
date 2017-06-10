package com.encodeering.conflate.experimental.jvm.co

import com.encodeering.conflate.experimental.test.any
import com.encodeering.conflate.experimental.test.mock
import com.encodeering.conflate.experimental.test.whenever
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext

/**
 * @author Michael Clausen - encodeering@gmail.com
 */

@RunWith (JUnitPlatform::class)
class RequeueContinuationTest : Spek ({

    describe ("RequeueException") {

        fun exception () = RequeueException("message")

        it ("should be a runtime exception") {
            expect (exception ()).to.satisfy { it is RuntimeException }
        }

    }

    describe ("RequeueContinuation") {

        class RequeueTestContinuation<in T> (
                continuation    : Continuation<T>,
                val immediately : Boolean,
                val success     : () -> Boolean
        ) : RequeueContinuation<T> (continuation) {

            override fun immediately () = immediately

            override fun requeue (call : () -> Unit) : Boolean = success ().apply { if (this) call () }

        }

        fun <T> immediate (continuation : Continuation<T>) = RequeueTestContinuation (continuation, true, { true })

        fun <T> requeue   (continuation : Continuation<T>, success : Boolean = true)  = RequeueTestContinuation (continuation, false, { success })

        fun <T> continuation () = mock<Continuation<T>> ()

        fun context () = mock<CoroutineContext> ()

        it ("uses the given coroutine context") {
            val continuation = continuation<Int> ()
            val context = context ()

            whenever (continuation.context).thenReturn (context)

            expect (immediate (continuation).context).to.equal (context)
            expect (requeue   (continuation).context).to.equal (context)
        }

        describe ("immediate") {

            it ("should resume") {
                val continuation = continuation<Int> ()

                immediate (continuation).resume (42)

                verify (continuation).resume (42)
                verify (continuation, never ()).resumeWithException (any ())
            }

            it ("should resume with an exception") {
                val continuation = continuation<Int> ()
                val exception = IllegalStateException ()

                immediate (continuation).resumeWithException (exception)

                verify (continuation, never ()).resume (any ())
                verify (continuation).resumeWithException (exception)
            }

        }


        describe ("requeue") {

            it ("should resume") {
                val continuation = continuation<Int> ()

                requeue (continuation, true).resume (42)

                verify (continuation).resume (42)
                verify (continuation, never ()).resumeWithException (any ())
            }

            it ("should resume with an exception") {
                val continuation = continuation<Int> ()
                val exception = IllegalStateException ()

                requeue (continuation, true).resumeWithException (exception)

                verify (continuation, never ()).resume (any ())
                verify (continuation).resumeWithException (exception)
            }

            it ("should resume with an exception if resume can't be requeued") {
                val continuation = continuation<Int> ()

                requeue (continuation, false).resume (42)

                verify (continuation, never ()).resume (any ())
                verify (continuation).resumeWithException (RequeueException("couldn't requeue '42' message on the looper"))
            }

            it ("should resume with an exception if resume-with-an-exception can't be requeued") {
                val continuation = continuation<Int> ()
                val exception = IllegalStateException ("oink")

                requeue (continuation, false).resumeWithException (exception)

                verify (continuation, never ()).resume (any ())
                verify (continuation).resumeWithException (RequeueException("couldn't requeue 'java.lang.IllegalStateException: oink' message on the looper"))
            }

        }

    }

})
