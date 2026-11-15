/*
 * SPDX-FileCopyrightText: 2018-2022 Friedrich von Never <friedrich@fornever.me>
 * SPDX-FileCopyrightText: 2026 file-link-executor contributors <https://github.com/ForNeVeR/file-link-executor>
 *
 * SPDX-License-Identifier: MIT
 */

package me.fornever.filelinkexecutor

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.terminal.JBTerminalSystemSettingsProviderBase
import com.intellij.util.io.URLUtil
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.FileSystemNotFoundException
import java.nio.file.Path
import java.util.regex.Matcher
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isExecutable

class CommandLinkProvider : ConsoleFilterProvider {

    companion object {
        private val logger = Logger.getInstance(CommandLinkProvider::class.java)
    }

    override fun getDefaultFilters(project: Project): Array<Filter> {
        return arrayOf(CommandLinkFilter(project))
    }

    class CommandLinkFilter(
        private val project: Project,
        private val editorColorsManager: Lazy<EditorColorsManager>,
        private val commandExecutor: Lazy<CommandExecutor>,
        private val virtualFileManager: Lazy<VirtualFileManager>
    ) : Filter, DumbAware {

        constructor(project: Project) : this(
            project,
            lazy { EditorColorsManager.getInstance() },
            lazy { CommandExecutor.getInstance(project) },
            lazy { VirtualFileManager.getInstance() }
        )

        private val highlightAttributes: TextAttributes
            get() {
                val scheme = editorColorsManager.value.globalScheme
                val commandToRun = scheme.getAttributes(JBTerminalSystemSettingsProviderBase.COMMAND_TO_RUN_USING_IDE_KEY)
                val link = scheme.getAttributes(CodeInsightColors.HYPERLINK_ATTRIBUTES)
                return TextAttributes.merge(commandToRun, link)
            }

        private fun isExecutable(path: Path): Boolean {
            return if (SystemInfo.isWindows) {
                val pathExtensions = PathEnvironmentVariableUtil.getWindowsExecutableFileExtensions()
                val extensionWithDot = "." + path.extension.lowercase()
                pathExtensions.contains(extensionWithDot)
            } else {
                path.isExecutable()
            }
        }

        private fun parsePath(url: String): Path? =
            try {
                Path.of(URI(url))
            } catch (e: URISyntaxException) {
                logger.warn("Cannot parse link \"$url\".", e)
                null
            } catch (e: IllegalArgumentException) {
                logger.warn("Cannot convert link \"$url\" to a local path.", e)
                null
            } catch (e: FileSystemNotFoundException) {
                logger.warn("Cannot convert link \"$url\" to a local path.", e)
                null
            }

        private fun buildHyperlinkInfo(url: String) = HyperlinkInfo {
            val path = parsePath(url) ?: return@HyperlinkInfo
            val isDirectory = path.isDirectory()
            val kind = if (isDirectory) "directory" else "file"
            logger.info("Link to a $kind \"$path\" was clicked")
            if (isDirectory)
                RevealFileAction.openDirectory(path)
            else {
                if (isExecutable(path)) {
                    logger.info("""File "$path" is executable, starting a program.""")
                    commandExecutor.value.runProgram(path)
                } else {
                    logger.info("""File "$path" is not executable, opening in editor.""")
                    val virtualFile = virtualFileManager.value.findFileByNioPath(path)
                    if (virtualFile == null) {
                        logger.warn("Virtual file is null for \"$path\".")
                        return@HyperlinkInfo
                    }

                    OpenFileDescriptor(project, virtualFile).navigate(true)
                }
            }
        }

        override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
            if (!URLUtil.canContainUrl(line) || !line.contains(URLUtil.FILE_PROTOCOL + URLUtil.SCHEME_SEPARATOR)) return null

            val pattern = URLUtil.FILE_URL_PATTERN
            val m = pattern.matcher(line)
            val items = mutableListOf<Filter.ResultItem>()
            val textStartOffset = entireLength - line.length
            while (m.find()) {
                items.add(createResultItem(textStartOffset, m))
            }

            return when {
                items.isEmpty() -> null
                else -> Filter.Result(items)
            }
        }

        private fun createResultItem(textStartOffset: Int, m: Matcher): Filter.ResultItem {
            val url = m.group()
            return Filter.ResultItem(
                textStartOffset + m.start(),
                textStartOffset + m.end(),
                buildHyperlinkInfo(url),
                highlightAttributes
            )
        }
    }
}
