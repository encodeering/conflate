package com.encodeering.conflate.experimental.epic.rx

import com.encodeering.conflate.experimental.api.Action
import com.encodeering.conflate.experimental.epic.Aspects
import com.encodeering.conflate.experimental.epic.Happenings
import com.encodeering.conflate.experimental.epic.Story.Aspect
import com.encodeering.conflate.experimental.epic.Story.Happening
import com.encodeering.conflate.experimental.epic.Story.Happening.Next
import com.encodeering.conflate.experimental.epic.just
import com.encodeering.conflate.experimental.epic.story.Book
import com.encodeering.conflate.experimental.test.any
import com.encodeering.conflate.experimental.test.fixture.Act
import com.encodeering.conflate.experimental.test.mock
import com.encodeering.conflate.experimental.test.spy
import com.encodeering.conflate.experimental.test.whenever
import com.winterbe.expekt.expect
import io.reactivex.Observable
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
@RunWith (JUnitPlatform::class)
class BookTest : Spek({

    describe ("Book") {

        fun book  (f : (Aspects<Action, Int>) -> Happenings) = Book.anecdote (f)

        describe ("Anecdote") {

            fun anecdote () = Book.anecdote<Int> ()

            it ("should not be a endless") {
                expect (anecdote ().endless).to.equal (false)
            }

            it ("should simply pass the action through") {
                val action = Act ("scott")
                val happening = anecdote ().embellish (just (Aspect (action, 42))).blockingLast ()

                expect (happening).to.equal (Next (action))
            }

        }

        describe ("Roman") {

            fun roman () = Book.roman<Int> ()

            it ("should be a endless") {
                expect (roman ().endless).to.equal (true)
            }

            it ("should simply pass the action through") {
                val action = Act ("scott")
                val happening = roman ().embellish (just (Aspect (action, 42))).blockingLast ()

                expect (happening).to.equal (Next (action))
            }
        }

        it ("should be embellish-able") {
            val action = Act ("ridley")
            val happening = book {
                it.map {
                    Next (Act ("${(it.action as Act).tor}-${it.state}"))
                }
            }.embellish(just (Aspect(action, 42))).blockingLast ()

            expect (happening.action).to.equal (Act ("ridley-42"))
        }

        it ("should be independent of a previous writing") {
            val cb = spy ()

            val       factory = mock<(Observable<Aspect<Action, Int>>) -> Observable<Happening>> ()
            whenever (factory.invoke (any ())).thenAnswer {
                just (it.arguments[0]).doOnNext { cb () }
            }

            val opening = Aspect(Act ("scott"), 42)

            val story = book (factory)
                story.embellish (just (opening)).blockingLast ()
                story.embellish (just (opening)).blockingLast ()

            verify (factory, times (2)).invoke (any ())
            verifyNoMoreInteractions (factory)

            verify (cb, times (2)).invoke ()
        }

    }

})