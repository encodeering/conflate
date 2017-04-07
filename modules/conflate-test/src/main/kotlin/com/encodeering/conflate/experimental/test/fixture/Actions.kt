package com.encodeering.conflate.experimental.test.fixture

import com.encodeering.conflate.experimental.api.Action

data class Act (val tor : String) : Action

data class Sub (val number : Int) : Action

data class Add (val number : Int) : Action