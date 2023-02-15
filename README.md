File Link Executor [![JetBrains Plugins][badge-plugins]][plugin-repository]
==================

This plugin will search through console output (including test output) for `file:///` links, and execute the links when they're clicked.

![Run console screenshot][docs.screenshot]

For executable files, they will be started, and the program output will be shown in the **Run** tool window.

For directory links, it will open the directories in a local file manager.

For non-executable files, they will open in editor. This differs from the standard IntelliJ file link processing in how it handles hashes in file URLs: for a link like `file:///file.txt#aaa`, IntelliJ will try opening a file `/file.txt#aaa`, while this plugin will ignore the part of the URL after the hash, and open `/file.txt`.

Works for any IntelliJ-based IDEs, starting from IntelliJ platform 2022.2.

Documentation
-------------

- [Changelog][docs.changelog]
- [The Contributor Guide][docs.contributing]
- [License (MIT)][docs.license]
- [Maintainership][docs.maintainership]

[badge-plugins]: https://img.shields.io/jetbrains/plugin/v/12787?label=file-link-executor
[docs.changelog]: CHANGELOG.md
[docs.contributing]: CONTRIBUTING.md
[docs.license]: LICENSE.md
[docs.maintainership]: MAINTAINERSHIP.md
[docs.screenshot]: docs/screenshot.png
[plugin-repository]: https://plugins.jetbrains.com/plugin/12787-file-link-executor
