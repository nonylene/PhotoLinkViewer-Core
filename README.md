PhotoLinkViewer-Core
===============

[![Circle CI](https://circleci.com/gh/nonylene/PhotoLinkViewer-Core.svg?style=svg)](https://circleci.com/gh/nonylene/PhotoLinkViewer-Core)

Google Play: [PhotoLinkViewer](https://play.google.com/store/apps/details?id=net.nonylene.photolinkviewer)

This is core module of PhotoLinkViewer. Twitter, Instagram OAuth, etc are not maintained here.

See also: [nonylene/PhotoLinkViewer](https://github.com/nonylene/PhotoLinkViewer)

This software is under GPL v2 licence, see LICENCE.md.

## install

### deprecated

This repository is used as submodule now. Repository on Github Pages will not be released.

### add repository

example:
```gradle
repositories {
    jcenter()
    maven {
        url "https://nonylene.github.io/PhotoLinkViewer-Core/repository"
    }
}
```

### add dependencies

```gradle
    compile "net.nonylene:photolinkviewer-core:{version}"
```

## notes

- If you want to launch this Activity from intent from other App, add this module's activity information in `AndroidManifest.xml`.