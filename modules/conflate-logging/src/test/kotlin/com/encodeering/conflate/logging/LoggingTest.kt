package com.encodeering.conflate.logging

import com.encodeering.conflate.api.Action
import com.encodeering.conflate.test.co
import com.encodeering.conflate.test.eq
import com.encodeering.conflate.test.fixture.Act
import com.encodeering.conflate.test.fixture.Middlewares.connection
import com.encodeering.conflate.test.mock
import com.encodeering.conflate.test.throws
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
@RunWith (JUnitPlatform::class)
class LoggingTest : Spek({

    describe ("Logging") {

        fun log () = mock<(String, Action) -> Unit> ()

        fun logging (before : Boolean = false, after : Boolean = false, exception : Boolean = false, log : (String, Action) -> Unit) = Logging<Unit> (
                before    = { before    },
                after     = { after     },
                exception = { exception },
                log = log
        )

        it ("should log before") {
            val action = Act ("scott")

            val log     = log ()
            val logging = logging (before = true, log = log)

            co {
                logging.interceptor (connection ()).dispatch (action)

                verify (log).invoke (">> {}", action)
                verifyNoMoreInteractions (log)
            }
        }

        it ("should log after") {
            val action = Act ("scott")

            val log     = log ()
            val logging = logging (after = true, log = log)

            co {
                logging.interceptor (connection ()).dispatch (action)

                verify (log).invoke ("-- {}", action)
                verifyNoMoreInteractions (log)
            }
        }

        it ("should log and rethrow exceptions") {
            val action = Act ("scott")

            val log     = log ()
            val logging = logging (exception = true, log = log)

            throws<IllegalStateException> {
                co {
                    try {
                        logging.interceptor (connection (next = { throw IllegalStateException () })).dispatch (action)
                    } finally {
                        verify (log).invoke ("!! {}", action)
                        verifyNoMoreInteractions (log)
                    }
                }
            }
        }

        it ("should call the next middleware") {
            val action = Act ("scott")

            val next    = mock<(Action)-> Unit> ()
            val logging = logging (exception = true, log = log ())

            co {
                logging.interceptor (connection (next = next)).dispatch (action)

                verify (next).invoke (action)
            }
        }

        it ("should call before, proceed and after in the correct order") {
            val action = Act ("scott")

            val next    = mock<(Action)-> Unit> ()
            val log     = log ()
            val logging = logging (before = true, after = true, log = log)

            co {
                logging.interceptor (connection (next = next)).dispatch (action)

                val ordered = Mockito.inOrder (log, next)
                    ordered.verify (log).invoke (eq (">> {}"), eq (action))
                    ordered.verify (next).invoke (action)
                    ordered.verify (log).invoke (eq ("-- {}"), eq (action))
            }
        }

    }

})
