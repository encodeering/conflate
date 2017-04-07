package com.encodeering.conflate.experimental.util

import com.encodeering.conflate.experimental.test.mock
import com.encodeering.conflate.experimental.test.whenever
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.Mockito.verify

/**
 * @author Michael Clausen - encodeering@gmail.com
 */

@RunWith (JUnitPlatform::class)
class ControlTest : Spek({

    describe ("trylog") {

        it ("should call the function") {
            val runnable = mock<Runnable> ()

            trylog (block = runnable::run)

            verify (runnable).run ()
        }

        it ("should catch runtime exceptions") {
            val runnable = mock<Runnable> ()

            whenever (runnable.run ()).thenThrow (IllegalStateException::class.java)

            trylog (block = runnable::run)

            verify (runnable).run ()
        }

        it ("should log the runtime exception") {
            val runnable = mock<Runnable> ()
            val consumer = mock<Runnable> ()

            val exception = IllegalStateException ()

            whenever (runnable.run ()).thenThrow (exception)

            trylog (log = { e -> expect (e).to.equal (exception); consumer.run () }, block = runnable::run)

            verify (consumer).run ()
            verify (runnable).run ()
        }

    }

})