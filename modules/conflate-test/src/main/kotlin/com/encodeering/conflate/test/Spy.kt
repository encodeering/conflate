package com.encodeering.conflate.test

import org.mockito.Mockito

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
       fun         <T> eq (value : T) : T = Mockito.eq (value)

inline fun <reified T> any () : T = Mockito.any (T::class.java)

inline fun <reified T> mock () : T = Mockito.mock (T::class.java)

       fun <T> whenever (element : T) = Mockito.`when` (element)

       fun spy () = mock<() -> Unit> ()

inline fun <reified T> spy () = mock<(T) -> Unit> ()