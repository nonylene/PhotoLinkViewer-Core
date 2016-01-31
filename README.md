PhotoLinkViewer-Core
===============

[![Circle CI](https://circleci.com/gh/nonylene/PhotoLinkViewer-Core.svg?style=svg)](https://circleci.com/gh/nonylene/PhotoLinkViewer-Core)

Google Play: [PhotoLinkViewer](https://play.google.com/store/apps/details?id=net.nonylene.photolinkviewer)

This is core module of PhotoLinkViewer. Twitter, Instagram OAuth, etc are not maintained here.

See also: [nonylene/PhotoLinkViewer](https://github.com/nonylene/PhotoLinkViewer)

This software is under GPL v2 licence, see LICENCE.md.

## install

### add repository

example:
```gradle
repositories {
    jcenter()
    maven {
        url "http://nonylene.github.io/PhotoLinkViewer-Core/repository"
    }
}
```

### add dependencies

```gradle
    compile "net.nonylene:photolinkviewer-core:0.0.1-SNAPSHOT"
```

## notes

- If you want to launch this Activity from intent from other App, add this module's activity information in `AndroidManifest.xml`.