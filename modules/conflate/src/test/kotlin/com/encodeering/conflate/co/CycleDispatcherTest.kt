package com.encodeering.conflate.co

import com.encodeering.conflate.api.Action
import com.encodeering.conflate.api.Dispatcher
import com.encodeering.conflate.test.fixture.Act
import com.encodeering.conflate.test.mock
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import kotlin.coroutines.experimental.EmptyCoroutineContext

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
@RunWith (JUnitPlatform::class)
class CycleDispatcherTest : Spek ({

    describe ("CycleDispatcher") {

        fun success  () = mock<Dispatcher.(Unit) -> Unit> ()

        fun failure  () = mock<Dispatcher.(Throwable) -> Unit> ()

        fun callback () = mock<(Action) -> Unit>  ()

        fun dispatcher (block : suspend (Action) -> Unit) = CycleDispatcher (EmptyCoroutineContext) { block (it) }

        describe ("dispatch") {

            it ("should dispatch an action properly") {
                val action = Act ("ridley")

                val          callback = callback ()
                dispatcher { callback (it) }.dispatch (action)

                verify (callback).invoke (action)
            }

        }

        describe ("completable") {

            describe ("success") {

                it ("should match the final state and scope") {
                    val action = Act ("ridley")

                    val success = success ()

                    val dispatcher = dispatcher { }
                        dispatcher.dispatch (action).then (fail = {}, ok = success)

                    verify (success).invoke (dispatcher, Unit)
                }

            }

            describe ("failure") {

                it ("should match the final state and scope") {
                    val action = Act ("ridley")

                    val failure = failure ()

                    val exception = IllegalStateException ()

                    val dispatcher = dispatcher { throw exception }
                        dispatcher.dispatch (action).then (fail = failure)

                    verify (failure).invoke (dispatcher, exception)
                }

            }

        }

    }

})