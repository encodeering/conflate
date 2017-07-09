# Conflate [![Build Status](https://travis-ci.org/encodeering/conflate.svg?branch=master)](https://travis-ci.org/encodeering/conflate) [![codecov](https://codecov.io/gh/encodeering/conflate/branch/master/graph/badge.svg)](https://codecov.io/gh/encodeering/conflate) [![Slack](https://img.shields.io/badge/Slack-conflate-orange.svg)](https://encodeering.slack.com/messages/conflate) [![Download](https://api.bintray.com/packages/encodeering/maven/conflate/images/download.svg)](https://bintray.com/encodeering/maven/conflate/_latestVersion) [![](https://jitpack.io/v/com.encodeering/conflate.svg)](https://jitpack.io/#com.encodeering/conflate)

An alternative [Redux](http://redux.js.org/) implementation written in [Kotlin](https://kotlinlang.org/)

## Setup

### Download

``` gradle
// build.gradle

repositories {
    maven {
        // Bintray
        url "http://dl.bintray.com/encodeering/maven"

        // Jitpack
        url "https://jitpack.io"
    }
}

dependencies {
    compile "com.encodeering.conflate:conflate-android:0.1.0"
    compile "com.encodeering.conflate:conflate-jfx:0.1.0"
    compile "com.encodeering.conflate:conflate-logging:0.1.0"
}
```

## Sample

### Android

* [Calenope](https://github.com/synyx/calenope) - A calendar application showing events from available Google accounts. 