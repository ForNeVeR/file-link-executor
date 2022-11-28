Changelog
=========
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [1.0.2] - 2022-11-29
### Fixed
- The **Run** tool window no longer get auto-activated on command start.

## [1.0.1] - 2022-11-23
### Fixed
- [#31: The progress is never removed](https://github.com/ForNeVeR/file-link-executor/issues/31).

## [1.0.0] - 2022-11-22
### Changed
- The tabs for started processes were moved to the standard **Run** tool window.
- The tabs created by the plugin are now automatically reused (so they won't pile up as before).
- The minimal IntelliJ version required is now 2022.2.
- Default link highlighting is now combined from hyperlink and "Command to run using IDE".
- The plugin no longer hijacks links to non-executable files.
- The filter is now available during indexing as well.

### Added
- An ability to open directories in file manager.
- Balloon notifications about script termination states.
- A progress indicator during script execution.

## [0.1.0] - 2020-06-21
### Fixed
- [#4: Tool window usage error after using the plugin in multiple projects open in the same IDE instance](https://github.com/ForNeVeR/file-link-executor/issues/4)

### Changed
- The minimal IntelliJ version required is now 2020.1.

## [0.0.6] - 2019-08-08
### Changed
- The plugin is renamed to the **File Link Executor**, to follow the plugin naming guidelines.

## [0.0.5] - 2019-07-28
### Changed
- The plugin is now compatible with IntelliJ-based IDEs based on the IntelliJ platform 2019.1+ (with no upper limit). This allows to load it in preview versions of the IDEs as well.

## [0.0.4] - 2019-07-28
### Fixed
- A plugin loading issue in a non-IDEA IntelliJ-based IDE.

## [0.0.3] - 2019-07-28
### Added
- Check if the file is executable before running it. If the file is not executable, it gets opened in the editor.

### Changed
- The minimal IntelliJ version required is now 2019.1 (up to, but excluding 2019.2).

## [0.0.2] - 2019-07-26
### Added
- The plugin is now compatible with all the IntelliJ-based IDEs of the corresponding version.

## [0.0.1] - 2018-10-17
The initial release of the IntelliJ Command Link plugin. It is only compatible with Rider 2017.3+.

[0.0.1]: https://github.com/ForNeVeR/file-link-executor/releases/tag/v0.0.1
[0.0.2]: https://github.com/ForNeVeR/file-link-executor/compare/v0.0.1...v0.0.2
[0.0.3]: https://github.com/ForNeVeR/file-link-executor/compare/v0.0.2...v0.0.3
[0.0.4]: https://github.com/ForNeVeR/file-link-executor/compare/v0.0.3...v0.0.4
[0.0.5]: https://github.com/ForNeVeR/file-link-executor/compare/v0.0.4...v0.0.5
[0.0.6]: https://github.com/ForNeVeR/file-link-executor/compare/v0.0.5...v0.0.6
[0.1.0]: https://github.com/ForNeVeR/file-link-executor/compare/v0.0.6...v0.1.0
[1.0.0]: https://github.com/ForNeVeR/file-link-executor/compare/v0.1.0...v1.0.0
[1.0.1]: https://github.com/ForNeVeR/file-link-executor/compare/v1.0.0...v1.0.1
[1.0.2]: https://github.com/ForNeVeR/file-link-executor/compare/v1.0.1...v1.0.2
[Unreleased]: https://github.com/ForNeVeR/file-link-executor/compare/v1.0.2...HEAD
