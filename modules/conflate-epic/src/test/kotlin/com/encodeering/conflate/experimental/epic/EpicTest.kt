package com.encodeering.conflate.experimental.epic

import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.epic.Story.Aspect
import com.encodeering.conflate.experimental.epic.Story.Happening
import com.encodeering.conflate.experimental.epic.Story.Happening.Initial
import com.encodeering.conflate.experimental.epic.Story.Happening.Next
import com.encodeering.conflate.experimental.test.any
import com.encodeering.conflate.experimental.test.co
import com.encodeering.conflate.experimental.test.fixture.Act
import com.encodeering.conflate.experimental.test.fixture.Middlewares.connection
import com.encodeering.conflate.experimental.test.mock
import com.encodeering.conflate.experimental.test.throws
import com.encodeering.conflate.experimental.test.whenever
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers.computation
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import java.util.concurrent.CountDownLatch

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
@RunWith (JUnitPlatform::class)
class EpicTest : Spek ({

    describe ("Epic") {

        fun story (daemon : Boolean = false, configure : (Action) -> Observable<out Happening>) =
            mock<Story<Unit>> ().apply {
                whenever (this.endless).thenReturn (daemon)
                whenever (this.embellish (any<Observable<Aspect<Unit>>> ())).thenAnswer {
                    @Suppress("UNCHECKED_CAST")
                    val value = it.arguments[0] as Observable<Aspect<Unit>>
                        value.flatMap { configure (it.action) }
                }
            }

        fun epic (vararg stories : Story<Unit>) = Epic (stories = * stories)

        describe ("dispatch") {

            describe ("next") {

                it ("should propagate one story two times") {
                    val story  = story { delayed (it).map (::Next) }

                    val action = Act ("scott")
                    val next   = mock<(Action) -> Unit> ()
                    val connection = connection (next = next)

                    co {
                        val interceptor = epic (story).interceptor (connection)
                            interceptor.dispatch (action)
                            interceptor.dispatch (action)
                    }

                    verify (next, times (2)).invoke (action)
                }

                it ("should propagate two stories one time") {
                    val storyA  = story { delayed (it).map (::Next) }
                    val storyB  = story { delayed (it).map (::Next) }

                    val action = Act ("scott")
                    val next   = mock<(Action) -> Unit> ()
                    val connection = connection (next = next)

                    co {
                        val epic = epic (storyA, storyB)
                            epic.interceptor (connection).dispatch (action)
                    }

                    verify (next, times (2)).invoke (action)
                }

            }

            describe ("initial") {

                it ("should initialize a new dispatch flow") {
                    val story  = story { delayed (it).map (::Initial) }

                    val action  = Act ("scott")
                    val initial = mock<(Action) -> Unit> ()
                    val connection = connection (initial = initial)

                    co {
                        val epic = epic (story)
                            epic.interceptor (connection).dispatch (action)
                    }

                    verify (initial).invoke (Act ("scott"))
                }

            }

            it ("should tell all stories even if one can't be told due some trolls") {
                val storyA  = story { just    (Act ("any")).map (::Next) }
                val storyB  = story { just    (it).map { throw IllegalStateException () }.map (::Next) }
                val storyC  = story { delayed (Act ("thing")).map (::Next) }

                val action = Act ("scott")
                val next   = mock<(Action) -> Unit> ()
                val connection = connection (next = next)

                throws<IllegalStateException> {
                    co {
                        val epic = epic (storyA, storyB, storyC)
                            epic.interceptor (connection).dispatch (action)
                    }
                }

                verify (next).invoke (Act ("any"))
                verify (next).invoke (Act ("thing"))
                verifyNoMoreInteractions (next)
            }

            it ("should create a new booklet for a non-endless") {
                val story  = story (daemon = false) { Observable.empty () }
                val action = Act ("scott")

                co {
                    val interceptor = epic (story).interceptor (connection ())
                        interceptor.dispatch (action)
                        interceptor.dispatch (action)
                }

                verify (story, times (2)).embellish (any ())
            }

            it ("should create one booklet for a endless") {
                val story  = story (daemon = true) { Observable.empty () }
                val action = Act ("scott")

                co {
                    val interceptor = epic (story).interceptor (connection ())
                        interceptor.dispatch (action)
                        interceptor.dispatch (action)

                    // it's fine to assert interpret's, but not to assert the story result
                }

                verify (story).embellish (any ())
            }

            it ("should not wait for a endless to finish") {
                val setup = CountDownLatch (1)
                val latch = CountDownLatch (1)

                val story  = story (daemon = true) { callable { setup.await (); it }.subscribeOn (computation ()).map (::Next).doFinally { latch.countDown () } }
                val action = Act ("scott")
                val next   = mock<(Action) -> Unit> ()

                co {
                    val epic = epic (story)
                        epic.interceptor (connection (next = next)).dispatch (action)
                }

                setup.countDown ()
                latch.await ()

                verify (next).invoke (action)
            }

            it ("should not close a booklet for a endless") {
                val latch = CountDownLatch (2)

                val story  = story (daemon = true) { just (it).map (::Next).doFinally { latch.countDown () } }
                val action = Act ("scott")
                val next   = mock<(Action) -> Unit> ()

                co {
                    val interceptor = epic (story).interceptor (connection (next = next))
                        interceptor.dispatch (action)
                        interceptor.dispatch (action)
                }

                latch.await ()

                verify (next, times (2)).invoke (action)
            }

        }

    }

})
