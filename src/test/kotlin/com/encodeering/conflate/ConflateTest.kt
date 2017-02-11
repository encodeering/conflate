package com.encodeering.conflate

import com.encodeering.conflate.api.Action
import com.encodeering.conflate.api.Middleware
import com.encodeering.conflate.api.Reducer
import com.encodeering.conflate.api.Storage
import com.encodeering.conflate.fixture.Add
import com.encodeering.conflate.fixture.any
import com.encodeering.conflate.fixture.eq
import com.encodeering.conflate.fixture.mock
import com.encodeering.conflate.fixture.whenever
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import java.util.Random
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.ContinuationInterceptor
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
@RunWith(JUnitPlatform::class)
class ConflateTest : Spek({

    describe ("Conflate") {

        fun accumulator (spy : (Action, Int) -> Unit = { _, _ -> Unit }) =
            object : Reducer<Int> {

                override fun reduce (action : Action, state : Int) : Int {
                    spy             (action, state)

                    return when     (action) {
                        is Add ->    action.number + state
                        else   ->                    state
                    }
                }

            }

        fun middleware (before : (Action, Storage<Int>, Middleware.Connection) -> Unit = { _, _, _ -> Unit },
                        after  : (Action, Storage<Int>, Middleware.Connection) -> Unit = { _, _, _ -> Unit }) =
            object : Middleware<Int> {

                suspend override fun dispatch (action : Action, storage : Storage<Int>, connection : Middleware.Connection) {
                    before                    (action,          storage,                connection)
                    connection.proceed        (action)
                    after                     (action,          storage,                connection)
                }

            }

        fun interceptor (spy : (Continuation<*>) -> Unit = { }) =
            object : AbstractCoroutineContextElement (ContinuationInterceptor), ContinuationInterceptor {

                override fun <T> interceptContinuation (continuation: Continuation<T>): Continuation<T> = continuation.apply { spy (this) }

            }

        fun conflate (context : CoroutineContext = EmptyCoroutineContext, initial : Int = Random ().nextInt (100), reducer : Reducer<Int> = accumulator (), vararg middleware : Middleware<Int>) =
                Conflate (
                    context = context,
                    initial = initial,
                    reducer = reducer,
                    middleware = * middleware
                )

        describe ("state") {

            it ("should return the initial state") {
                expect (conflate (initial = 1337).state).to.equal (1337)
            }

            it ("should return the new state after the reduction process") {
                val conflate = conflate(initial = 1337)
                    conflate.dispatcher.dispatch (Add (42))

                expect (conflate.state).to.equal (1337 + 42)
            }

        }

        describe ("dispatcher") {

            it ("should start a cycle for each dispatch in order") {
                val reducer = mock<(Action, Int) -> Unit> ()

                val conflate = conflate (initial = 1337, reducer = accumulator (reducer))
                    conflate.dispatcher.apply {
                        dispatch (Add (42))
                        dispatch (Add (43))
                        dispatch (Add (44))
                    }

                val ordered = inOrder (reducer)
                    ordered.verify (reducer).invoke (eq (Add (42)), eq (1337))
                    ordered.verify (reducer).invoke (eq (Add (43)), eq (1337 + 42))
                    ordered.verify (reducer).invoke (eq (Add (44)), eq (1337 + 42 + 43))
                    ordered.verifyNoMoreInteractions ()
            }

            it ("should use the provided co-routine context") {
                val context = mock<(Continuation<*>) -> Unit> ()

                val conflate = conflate (context = interceptor (context))
                    conflate.dispatcher.dispatch (Add (0))

                verify (context).invoke (any ())
            }

            xit ("should support async beginnings") {

            }

        }

        describe ("middleware") {

            it ("should be called once a cycle") {
                val spy = mock<(Action, Storage<Int>, Middleware.Connection) -> Unit> ()

                val conflate = conflate (middleware = middleware (before = spy))

                conflate.dispatcher.dispatch (Add (0))

                verify (spy).invoke  (eq (Add (0)), eq (conflate), any ())
                verifyNoMoreInteractions (spy)
            }

            it ("should preserve the given middleware order") {
                val first  = mock<(Action, Storage<Int>, Middleware.Connection) -> Unit> ()
                val second = mock<(Action, Storage<Int>, Middleware.Connection) -> Unit> ()

                val conflate = conflate (middleware = * arrayOf (
                        middleware (before = first),
                        middleware (before = second)
                ))

                conflate.dispatcher.dispatch (Add (0))

                val ordered = inOrder (first, second)
                    ordered.verify (first).invoke  (eq (Add (0)), eq (conflate), any ())
                    ordered.verify (second).invoke (eq (Add (0)), eq (conflate), any ())
            }

        }

        describe ("subscription") {

            it ("should be called after a cycle only") {
                val listener = mock<() -> Unit> ()

                val conflate = conflate ()
                    conflate.subscribe (listener)

                verify (listener, never ()).invoke ()
            }

            it ("should be called after the reduction process") {
                val reducer  = mock<(Action, Int) -> Unit> ()
                val listener = mock<() -> Unit> ()

                val conflate = conflate (reducer = accumulator (reducer))
                    conflate.subscribe (listener)
                    conflate.dispatcher.dispatch (Add (0))

                val ordered = inOrder (reducer, listener)
                    ordered.verify (reducer).invoke (any (), any ())
                    ordered.verify (listener).invoke ()
                    ordered.verifyNoMoreInteractions ()
            }

            it ("should provide an unsubscription handle") {
                val one = mock<() -> Unit> ()
                val two = mock<() -> Unit> ()

                val              conflate = conflate ()
                val unregister = conflate.subscribe (one)
                                 conflate.subscribe (two)

                conflate.dispatcher.run {
                    dispatch (Add (0))
                    dispatch (Add (0))
                }

                verify (one, times (2)).invoke ()
                verify (two, times (2)).invoke ()

                unregister ()

                conflate.dispatcher.run {
                    dispatch (Add (0))
                    dispatch (Add (0))
                }

                verifyNoMoreInteractions (one)
                verify (two, times (4)).invoke ()
            }

            it ("should guard a listeners to prevent poison pills") {
                val one = mock<() -> Unit> ()
                val two = mock<() -> Unit> ()

                whenever (one.invoke ()).thenThrow (IllegalStateException ())

                val conflate = conflate ()
                    conflate.subscribe (one)
                    conflate.subscribe (two)
                    conflate.dispatcher.dispatch (Add (0))

                verify (one).invoke ()
                verify (two).invoke ()
            }

        }

    }

})