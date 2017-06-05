# Conflate [![Build Status](https://travis-ci.org/encodeering/conflate.svg?branch=master)](https://travis-ci.org/encodeering/conflate) [![Download](https://api.bintray.com/packages/encodeering/maven/conflate/images/download.svg)](https://bintray.com/encodeering/maven/conflate/_latestVersion)

An alternative [Redux](http://redux.js.org/) implementation written in [Kotlin](https://kotlinlang.org/)

## Setup

### Bintray

``` gradle
// build.gradle

repositories {
    maven {
        url "http://dl.bintray.com/encodeering/maven"
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