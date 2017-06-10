package com.encodeering.conflate.experimental.jvm

import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.api.Middleware
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import kotlin.coroutines.experimental.Continuation

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
@org.junit.runner.RunWith(org.junit.platform.runner.JUnitPlatform::class)
class ConflateTest : org.jetbrains.spek.api.Spek({

    describe ("Conflate") {

        fun accumulator (spy : (com.encodeering.conflate.experimental.api.Action, Int) -> Unit = { _, _ -> Unit }) =
                com.encodeering.conflate.experimental.test.fixture.Reducers.accumulator(spy)

        fun middleware (before : (com.encodeering.conflate.experimental.api.Action, com.encodeering.conflate.experimental.api.Middleware.Connection<Int>) -> Unit = { _, _ -> Unit },
                        after  : (com.encodeering.conflate.experimental.api.Action, com.encodeering.conflate.experimental.api.Middleware.Connection<Int>) -> Unit = { _, _ -> Unit }) =
                com.encodeering.conflate.experimental.test.fixture.Middlewares.middleware(before, after)

        fun interceptor (spy : (kotlin.coroutines.experimental.Continuation<*>) -> Unit = { }) =
            object : kotlin.coroutines.experimental.AbstractCoroutineContextElement(kotlin.coroutines.experimental.ContinuationInterceptor), kotlin.coroutines.experimental.ContinuationInterceptor {

                override fun <T> interceptContinuation (continuation: kotlin.coroutines.experimental.Continuation<T>): kotlin.coroutines.experimental.Continuation<T> = continuation.apply { spy (this) }

            }

        fun conflate (context : kotlin.coroutines.experimental.CoroutineContext = kotlin.coroutines.experimental.EmptyCoroutineContext, initial : Int = java.util.Random().nextInt (100), reducer : com.encodeering.conflate.experimental.api.Reducer<Int> = accumulator (), vararg middleware : com.encodeering.conflate.experimental.api.Middleware<Int>) =
                com.encodeering.conflate.experimental.jvm.Conflate(
                        context = context,
                        initial = initial,
                        reducer = reducer,
                        middleware = * middleware
                )

        describe ("construction") {

            it ("provides a context free solution") {
                com.winterbe.expekt.expect(com.encodeering.conflate.experimental.jvm.Conflate(42, accumulator()).state).to.equal (42)
            }

        }

        describe ("state") {

            it ("should return the initial state") {
                com.winterbe.expekt.expect(conflate(initial = 1337).state).to.equal (1337)
            }

            it ("should return the new state after the reduction process") {
                val conflate = conflate(initial = 1337)
                    conflate.dispatcher.dispatch (com.encodeering.conflate.experimental.test.fixture.Add(42))

                com.winterbe.expekt.expect(conflate.state).to.equal (1337 + 42)
            }

        }

        describe ("dispatcher") {

            it ("should start a cycle for each dispatch in order") {
                val reducer = com.encodeering.conflate.experimental.test.mock<(Action, Int) -> Unit>()

                val conflate = conflate (initial = 1337, reducer = accumulator (reducer))
                    conflate.dispatcher.apply {
                        dispatch (com.encodeering.conflate.experimental.test.fixture.Add(42))
                        dispatch (com.encodeering.conflate.experimental.test.fixture.Add(43))
                        dispatch (com.encodeering.conflate.experimental.test.fixture.Add(44))
                    }

                val ordered = inOrder (reducer)
                    ordered.verify (reducer).invoke (com.encodeering.conflate.experimental.test.eq(com.encodeering.conflate.experimental.test.fixture.Add(42)), com.encodeering.conflate.experimental.test.eq(1337))
                    ordered.verify (reducer).invoke (com.encodeering.conflate.experimental.test.eq(com.encodeering.conflate.experimental.test.fixture.Add(43)), com.encodeering.conflate.experimental.test.eq(1337 + 42))
                    ordered.verify (reducer).invoke (com.encodeering.conflate.experimental.test.eq(com.encodeering.conflate.experimental.test.fixture.Add(44)), com.encodeering.conflate.experimental.test.eq(1337 + 42 + 43))
                    ordered.verifyNoMoreInteractions ()
            }

            it ("should use the provided co-routine context") {
                val context = com.encodeering.conflate.experimental.test.mock<(Continuation<*>) -> Unit>()

                val conflate = conflate (context = interceptor (context))
                    conflate.dispatcher.dispatch (com.encodeering.conflate.experimental.test.fixture.Add(0))

                verify (context).invoke (com.encodeering.conflate.experimental.test.any())
            }

            xit ("should support async beginnings") {

            }

        }

        describe ("middleware") {

            it ("should be called once a cycle") {
                val spy = com.encodeering.conflate.experimental.test.mock<(Action, Middleware.Connection<Int>) -> Unit>()

                val conflate = conflate (middleware = middleware (before = spy))

                conflate.dispatcher.dispatch (com.encodeering.conflate.experimental.test.fixture.Add(0))

                verify (spy).invoke  (com.encodeering.conflate.experimental.test.eq(com.encodeering.conflate.experimental.test.fixture.Add(0)), com.encodeering.conflate.experimental.test.any())
                verifyNoMoreInteractions (spy)
            }

            it ("should preserve the given middleware order") {
                val first  = com.encodeering.conflate.experimental.test.mock<(Action, Middleware.Connection<Int>) -> Unit>()
                val second = com.encodeering.conflate.experimental.test.mock<(Action, Middleware.Connection<Int>) -> Unit>()

                val conflate = conflate (middleware = * arrayOf (
                        middleware (before = first),
                        middleware (before = second)
                ))

                conflate.dispatcher.dispatch (com.encodeering.conflate.experimental.test.fixture.Add(0))

                val ordered = inOrder (first, second)
                    ordered.verify (first).invoke  (com.encodeering.conflate.experimental.test.eq(com.encodeering.conflate.experimental.test.fixture.Add(0)), com.encodeering.conflate.experimental.test.any())
                    ordered.verify (second).invoke (com.encodeering.conflate.experimental.test.eq(com.encodeering.conflate.experimental.test.fixture.Add(0)), com.encodeering.conflate.experimental.test.any())
            }

        }

        describe ("subscription") {

            it ("should be called after a cycle only") {
                val listener = com.encodeering.conflate.experimental.test.mock<() -> Unit>()

                val conflate = conflate ()
                    conflate.subscribe (listener)

                verify (listener, never ()).invoke ()
            }

            it ("should be called after the reduction process") {
                val reducer  = com.encodeering.conflate.experimental.test.mock<(Action, Int) -> Unit>()
                val listener = com.encodeering.conflate.experimental.test.mock<() -> Unit>()

                val conflate = conflate (reducer = accumulator (reducer))
                    conflate.subscribe (listener)
                    conflate.dispatcher.dispatch (com.encodeering.conflate.experimental.test.fixture.Add(0))

                val ordered = inOrder (reducer, listener)
                    ordered.verify (reducer).invoke (com.encodeering.conflate.experimental.test.any(), com.encodeering.conflate.experimental.test.any())
                    ordered.verify (listener).invoke ()
                    ordered.verifyNoMoreInteractions ()
            }

            it ("should provide an unsubscription handle") {
                val one = com.encodeering.conflate.experimental.test.mock<() -> Unit>()
                val two = com.encodeering.conflate.experimental.test.mock<() -> Unit>()

                val              conflate = conflate ()
                val unregister = conflate.subscribe (one)
                                 conflate.subscribe (two)

                conflate.dispatcher.run {
                    dispatch (com.encodeering.conflate.experimental.test.fixture.Add(0))
                    dispatch (com.encodeering.conflate.experimental.test.fixture.Add(0))
                }

                verify (one, times (2)).invoke ()
                verify (two, times (2)).invoke ()

                unregister.run ()

                conflate.dispatcher.run {
                    dispatch (com.encodeering.conflate.experimental.test.fixture.Add(0))
                    dispatch (com.encodeering.conflate.experimental.test.fixture.Add(0))
                }

                verifyNoMoreInteractions (one)
                verify (two, times (4)).invoke ()
            }

            it ("should guard a listeners to prevent poison pills") {
                val one = com.encodeering.conflate.experimental.test.mock<() -> Unit>()
                val two = com.encodeering.conflate.experimental.test.mock<() -> Unit>()

                com.encodeering.conflate.experimental.test.whenever(one.invoke()).thenThrow (IllegalStateException ())

                val conflate = conflate ()
                    conflate.subscribe (one)
                    conflate.subscribe (two)
                    conflate.dispatcher.dispatch (com.encodeering.conflate.experimental.test.fixture.Add(0))

                verify (one).invoke ()
                verify (two).invoke ()
            }

        }

    }

})