package com.encodeering.conflate.api

/**
 * @author Michael Clausen - encodeering@gmail.com
 */
interface Dispatcher {

    fun dispatch (action : Action) : Completable<Dispatcher, Unit>

}