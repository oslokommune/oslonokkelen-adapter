Oslonøkkelen adapter
====================


Development
-----------
Please install the Ktlint git pre-commit hook to auto format changed files.

    ./gradlew addKtlintFormatGitPreCommitHook

Build docs
----------
We use [asciidoc](https://asciidoctor.org/docs/asciidoc-writers-guide/) for our documentation. This can be rendered
as html by running:

    ./gradlew :adapter-docs:asciidoctor
