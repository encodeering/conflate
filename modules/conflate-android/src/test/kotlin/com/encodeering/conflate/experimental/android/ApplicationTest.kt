package com.encodeering.conflate.experimental.android

import android.app.Application
import android.content.pm.ApplicationInfo
import com.encodeering.conflate.experimental.android.co.Looper
import com.encodeering.conflate.experimental.api.Dispatcher
import com.encodeering.conflate.experimental.jvm.co.CycleDispatcher
import com.encodeering.conflate.experimental.jvm.middleware.Noop
import com.encodeering.conflate.experimental.logging.Logging
import com.encodeering.conflate.experimental.test.fixture.Reducers
import com.encodeering.conflate.experimental.test.whenever
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.Mockito.spy
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

        fun application (configure : ApplicationInfo.() -> Unit) : Application {
            val info = ApplicationInfo ()

            configure (info)

            val       application = spy (Application ())
            whenever (application.applicationInfo).thenReturn (info)

            return application
        }

        fun conflate ()
            = application { ApplicationInfo () }.conflate (42, Reducers.accumulator ())

        fun conflate (context : CoroutineContext)
            = application { ApplicationInfo () }.conflate (42, Reducers.accumulator (), context)

        describe ("Conflate") {

            it ("shall prefer a given interceptor") {
                expect (conflate (Proxy).dispatcher.context ()[ContinuationInterceptor]).to.equal (Proxy)
            }

            it ("shall inject a proper interceptor otherwise") {
                expect (conflate ().dispatcher.context ()[ContinuationInterceptor]).to.equal (Looper)
                expect (conflate (EmptyCoroutineContext).dispatcher.context ()[ContinuationInterceptor]).to.equal (Looper)
            }

        }

        describe ("Logging") {

            it ("shall return a logger if the application is debuggable") {
                expect (application { flags = ApplicationInfo.FLAG_DEBUGGABLE }.logging<Any> ()).to.satisfy { it is Logging }
            }

            it ("shall return a noop otherwise") {
                expect (application { flags = 0 }.logging<Any> ()).to.satisfy { it is Noop }
            }

        }

        describe ("Debuggable") {

            it ("shall read the application info flag") {
                expect (application { flags = ApplicationInfo.FLAG_DEBUGGABLE }.debuggable ()).to.equal (true)
                expect (application { flags = 0 }.debuggable ()).to.equal (false)
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