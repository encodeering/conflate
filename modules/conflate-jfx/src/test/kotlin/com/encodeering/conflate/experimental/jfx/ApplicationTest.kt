package com.encodeering.conflate.experimental.jfx

import com.encodeering.conflate.experimental.api.Dispatcher
import com.encodeering.conflate.experimental.jfx.co.FxAT
import com.encodeering.conflate.experimental.jvm.co.CycleDispatcher
import com.encodeering.conflate.experimental.logging.Logging
import com.encodeering.conflate.experimental.test.fixture.Reducers
import com.encodeering.conflate.experimental.test.mock
import com.winterbe.expekt.expect
import javafx.application.Application
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.ContinuationInterceptor
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
@RunWith(JUnitPlatform::class)
class ApplicationTest : Spek ({

    describe ("Application") {

        fun application () : Application {
            val    application = mock<Application> ()

            return application
        }

        fun conflate ()
            = application ().conflate (42, Reducers.accumulator ())

        fun conflate (context : CoroutineContext)
            = application ().conflate (42, Reducers.accumulator (), context)

        describe ("Conflate") {

            it ("shall prefer a given interceptor") {
                expect (conflate (Proxy).dispatcher.context ()[ContinuationInterceptor]).to.equal (Proxy)
            }

            it ("shall inject a proper interceptor otherwise") {
                expect (conflate ().dispatcher.context ()[ContinuationInterceptor]).to.equal (FxAT)
                expect (conflate (EmptyCoroutineContext).dispatcher.context ()[ContinuationInterceptor]).to.equal (FxAT)
            }

        }

        describe ("Logging") {

            it ("shall return a logger") {
                expect (application ().logging<Any> ()).to.satisfy { it is Logging }
            }

        }

    }

})

private fun Dispatcher.context () : CoroutineContext {
    val field = CycleDispatcher::class.java.getDeclaredField ("context")
        field.isAccessible = true

    return field.get (this) as CoroutineContext
}

object Proxy : AbstractCoroutineContextElement (ContinuationInterceptor), ContinuationInterceptor {

    override fun <T> interceptContinuation (continuation : Continuation<T>) = continuation

}