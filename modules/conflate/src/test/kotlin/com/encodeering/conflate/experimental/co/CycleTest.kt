package com.encodeering.conflate.experimental.co

import com.encodeering.conflate.experimental.test.any
import com.encodeering.conflate.experimental.test.co
import com.encodeering.conflate.experimental.test.eq
import com.encodeering.conflate.experimental.test.mock
import com.encodeering.conflate.experimental.test.whenever
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import java.util.Random
import kotlin.coroutines.experimental.EmptyCoroutineContext

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
@RunWith (JUnitPlatform::class)
class CycleTest : Spek ({

    describe ("Cycle") {

        describe ("notification") {

            fun success () = mock<Int.(Int) -> Unit> ()

            fun failure () = mock<Int.(Throwable) -> Unit> ()

            fun cycle (scope : Int = Random ().nextInt (100)) = Cycle<Int, Int> (context = EmptyCoroutineContext, scope = scope)

            fun <T> watchmen (count : Int = 10, mock : () -> T) = (0.until (count)).map { mock () }

            describe ("pre-bound listener") {

                it ("should be informed once with the very first value") {
                    val cycle = cycle ()

                    val watchmen = watchmen { success () }
                        watchmen.forEach { cycle.then (fail = {}, ok = it) }

                    co {
                        cycle.resume (42)
                        cycle.resume (42 * 2)

                        watchmen.forEach {
                            verify (it).invoke (any<Int> (), eq (42))
                            verifyNoMoreInteractions (it)
                        }
                    }
                }

                it ("should be informed once with the very first error") {
                    val cycle = cycle ()

                    val watchmen = watchmen { failure () }
                        watchmen.forEach { cycle.then (fail = it) }

                    val exception = IllegalStateException ()

                    co {
                        cycle.resumeWithException (exception)
                        cycle.resumeWithException (IllegalArgumentException ())

                        watchmen.forEach {
                            verify (it).invoke (any<Int> (), eq (exception))
                            verifyNoMoreInteractions (it)
                        }
                    }
                }

            }

            describe ("post-bound listener") {

                it ("should be informed once with the very first value") {
                    val cycle = cycle ()

                    val watchmen = watchmen { success () }

                    co {
                        cycle.resume (42)
                        cycle.resume (42 * 2)

                        watchmen.forEach { cycle.then (fail = {}, ok = it) }
                        watchmen.forEach {
                            verify (it).invoke (any<Int> (), eq (42))
                            verifyNoMoreInteractions (it)
                        }
                    }
                }

                it ("should be informed once with the very first error") {
                    val cycle = cycle ()

                    val watchmen = watchmen { failure () }

                    val exception = IllegalStateException ()

                    co {
                        cycle.resumeWithException (exception)
                        cycle.resumeWithException (IllegalArgumentException ())

                        watchmen.forEach { cycle.then (fail = it) }
                        watchmen.forEach {
                            verify (it).invoke (any<Int> (), eq (exception))
                            verifyNoMoreInteractions (it)
                        }
                    }
                }

            }

            describe ("success listener") {

                it ("should be guarded to prevent poison pills") {
                    val success = success ()

                    val cycle = cycle ()
                        cycle.then (fail = {}, ok = success)

                    whenever (success.invoke (any (), any ())).thenThrow (IllegalArgumentException ())

                    co {
                        cycle.resume (42)

                        verify (success).invoke (any (), any ())
                    }
                }

                it ("should be invoked with an appropriate scope") {
                    val success = success ()

                    val cycle = cycle (1337)
                        cycle.then (fail = {}, ok = success)

                    co {
                        cycle.resume (42)

                        verify (success).invoke (1337, 42)
                    }
                }

            }

            describe ("failure ()listener") {

                it ("should be guarded to prevent poison pills") {
                    val error = failure ()

                    val cycle = cycle ()
                        cycle.then (fail = error)

                    whenever (error.invoke (any (), any ())).thenThrow (IllegalArgumentException ())

                    co {
                        cycle.resumeWithException (IllegalStateException ())

                        verify (error).invoke (any (), any ())
                    }
                }

                it ("should be invoked with an appropriate scope") {
                    val error = failure ()

                    val cycle = cycle (1337)
                        cycle.then (fail = error)

                    val exception = IllegalStateException()

                    co {
                        cycle.resumeWithException (exception)

                        verify (error).invoke (1337, exception)
                    }
                }

            }

        }

    }

})