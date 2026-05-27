<!--
SPDX-FileCopyrightText: 2022-2026 Friedrich von Never <friedrich@fornever.me>

SPDX-License-Identifier: MIT
-->

The Contributor Guide
=====================

Build
-----

To build the plugin, execute the following shell command:

```console
$ ./gradlew buildPlugin
```

The plugin distribution will be built in `build/distributions`. It is possible to install it using **Install Plugin From Disk** action in any supported IDE.

Run
---

To run an automatically downloaded IntelliJ instance with the development version of the plugin, execute the following shell command:

```console
$ ./gradlew runIde
```

<!-- REUSE-IgnoreStart -->
License Automation
------------------

If the CI asks you to update the file licenses, follow one of these:
1. Update the headers manually (look at the existing files), something like this:
   ```fsharp
   // SPDX-FileCopyrightText: %year% %your name% <%your contact info, e.g. email%>
   //
   // SPDX-License-Identifier: MIT
   ```
   (accommodate to the file's comment style if required).
2. Alternately, use the [REUSE][reuse] tool:
   ```console
   $ reuse annotate --license MIT --copyright '%your name% <%your contact info, e.g. email%>' %file names to annotate%
   ```

(Feel free to attribute the changes to "file-link-executor contributors <https://github.com/ForNeVeR/file-link-executor>" instead of your name in a multi-author file, or if you don't want your name to be mentioned in the project's source: this doesn't mean you'll lose the copyright.)

<!-- REUSE-IgnoreEnd -->

[reuse]: https://reuse.software/
