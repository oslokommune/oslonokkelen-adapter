Oslonøkkelen adapter
====================
[![](https://jitpack.io/v/oslokommune/oslonokkelen-adapter.svg)](https://jitpack.io/#oslokommune/oslonokkelen-adapter)
[![Testing](https://github.com/oslokommune/oslonokkelen-adapter/actions/workflows/testing.yml/badge.svg)](https://github.com/oslokommune/oslonokkelen-adapter/actions/workflows/testing.yml)

Development
-----------
Please install the Ktlint git pre-commit hook to auto format changed files.

    ./gradlew addKtlintFormatGitPreCommitHook

How to upgrade gradle wrapper
-----------
Find the newest version of gradle here: https://gradle.org/releases/ 
Then run this command(remember to replace version with the newest version):

``` bash
./gradlew wrapper --gradle-version 7.6
```

Build docs
----------
We use [asciidoc](https://asciidoctor.org/docs/asciidoc-writers-guide/) for our documentation. This can be rendered
as html by running:

    ./gradlew :adapter-docs:asciidoctor

This will generate new html files you have to commit and push. 