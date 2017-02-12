package com.encodeering.conflate.test.fixture

import com.encodeering.conflate.api.Action

data class Act (val tor : String) : Action

data class Add (val number : Int) : Action