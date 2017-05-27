package com.encodeering.conflate.experimental.api

/**
 * An action usually describes an intent that something should happen and may or may not cause other intents to be raised
 * along the way to the storage, which is usually managed by chained middleware components.
 *
 * Intents should be designed with immutability in mind, or at least be effectively final.
 * Even though mutable properties are acceptable for cases with no adequate default value,
 * they should be used sparingly and never be mutated, as there will be no implicit synchronization mechanism implemented.
 * It's further recommended to use serializable payloads, as they make the intents more reasonable and debug friendly.
 *
 * As a short reminder:
 *
 * - Intents should *never* be mutated
 * - Intents interacting with the store *must* be serializable, but those being processed by middleware components only *are not* required to be fully serializable
 *
 * ### Official documentation:
 *
 * - Actions are payloads of information that send data from your application to your store.
 * They are the only source of information for the store. [http://redux.js.org/docs/basics/Actions.html]
 *
 * - As with state, serializable actions enable several of Redux's defining features, such as time travel debugging, and recording and replaying actions. [http://redux.js.org/docs/faq/Actions.html#actions]
 *
 * ### Sample
 *
 * #### Intent with default parameters
 * ```
 * data class Toast (
 *     val message  : String,
 *     val duration : Int = LENGTH_SHORT
 * ) : Action
 *
 * dispatch (Toast ("welcome $user"))
 * ```
 *
 * #### Intent with a UI toggle to display a progress indicator
 *
 * ```
 * data class SearchAccommodation (
 *     val city           : String,                      // title and query parameter
 *     val accommodations : List<String> = emptyList (), // result
 *     val searching      : Boolean      = true          // progress indicator
 * ) : Action
 *
 * val       search = SearchAccommodation ("Berlin")
 * dispatch (search)
 * dispatch (search.copy (accommodations = listOf ("Hotel XYZ"), searching = false))
 * ```
 *
 * @author Michael Clausen - encodeering@gmail.com
 */
interface Action