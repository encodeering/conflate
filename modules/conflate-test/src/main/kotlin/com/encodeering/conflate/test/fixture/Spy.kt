package com.encodeering.conflate.test.fixture

import org.mockito.Mockito

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
inline fun         <T> eq (value : T) : T = Mockito.eq (value)

inline fun <reified T> any () : T = Mockito.any (T::class.java)

inline fun <reified T> mock () : T = Mockito.mock (T::class.java)

       fun <T> whenever (element : T) = Mockito.`when` (element)