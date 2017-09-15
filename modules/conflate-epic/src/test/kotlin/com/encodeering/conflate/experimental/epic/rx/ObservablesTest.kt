package com.encodeering.conflate.experimental.epic.rx

import com.encodeering.conflate.experimental.epic.delayed
import com.encodeering.conflate.experimental.test.any
import com.encodeering.conflate.experimental.test.co
import com.encodeering.conflate.experimental.test.spy
import com.encodeering.conflate.experimental.test.throws
import com.encodeering.conflate.experimental.test.whenever
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.Mockito.inOrder

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
@RunWith(JUnitPlatform::class)
class ObservablesTest : Spek({

    describe ("Observables") {

        describe ("async") {

            it ("should pass all values") {
                val cb = spy<Int> ()
                val block : suspend (Int) -> Unit = { cb (it) }

                delayed (1, 2, 3).async (f = block).blockingLast ()
                cb (99)

                val ordered = inOrder (cb)
                    ordered.verify (cb).invoke (1)
                    ordered.verify (cb).invoke (2)
                    ordered.verify (cb).invoke (3)
                    ordered.verify (cb).invoke (99)
                    ordered.verifyNoMoreInteractions ()
            }

            it ("should pass an exception") {
                val       cb = spy<Int> ()
                whenever (cb.invoke (any<Int> ())).thenThrow (IllegalStateException::class.java)

                val block : suspend (Int) -> Unit = { cb (it) }

                throws<IllegalStateException> {
                    delayed (1, 2, 3).async (f = block).blockingLast ()
                    cb (99)
                }

                val ordered = inOrder (cb)
                    ordered.verify (cb).invoke (1)
                    ordered.verifyNoMoreInteractions ()
            }

        }

        describe ("await") {

            it ("should wait until the observable completes") {
                val cb = spy<Int> ()

                co {
                    delayed (1, 2, 3).await (f = cb)
                    cb (99)
                }

                val ordered = inOrder (cb)
                    ordered.verify (cb).invoke (1)
                    ordered.verify (cb).invoke (2)
                    ordered.verify (cb).invoke (3)
                    ordered.verify (cb).invoke (99)
                    ordered.verifyNoMoreInteractions ()
            }

            it ("should circuit on exception") {
                val       cb = spy<Int> ()
                whenever (cb.invoke(any<Int> ())).thenThrow (IllegalStateException::class.java)

                throws<IllegalStateException> {
                    co {
                        delayed (1, 2, 3).await (f = cb)
                        cb (99)
                    }
                }

                val ordered = inOrder (cb)
                    ordered.verify (cb).invoke (1)
                    ordered.verifyNoMoreInteractions ()
            }

        }

    }

})