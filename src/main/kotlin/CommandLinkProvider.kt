package me.fornever.filelinkexecutor

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.terminal.JBTerminalSystemSettingsProviderBase
import com.intellij.util.io.URLUtil
import java.io.File
import java.net.URL
import java.util.regex.Matcher

class CommandLinkProvider : ConsoleFilterProvider {

    companion object {
        private val logger = Logger.getInstance(CommandLinkProvider::class.java)
    }

    override fun getDefaultFilters(project: Project): Array<Filter> {
        return arrayOf(CommandLinkFilter(project))
    }

    inner class CommandLinkFilter(
        private val editorColorsManager: Lazy<EditorColorsManager>,
        private val commandExecutor: Lazy<CommandExecutor>,
        private val localFileSystem: Lazy<LocalFileSystem>,
        private val fileEditorManager: Lazy<FileEditorManager>
    ) : Filter {

        constructor(project: Project) : this(
            lazy { EditorColorsManager.getInstance() },
            lazy { CommandExecutor.getInstance(project) },
            lazy { LocalFileSystem.getInstance() },
            lazy { FileEditorManager.getInstance(project) }
        )

        private val highlightAttributes: TextAttributes
            get() {
                val scheme = editorColorsManager.value.globalScheme
                val commandToRun = scheme.getAttributes(JBTerminalSystemSettingsProviderBase.COMMAND_TO_RUN_USING_IDE_KEY)
                val link = scheme.getAttributes(CodeInsightColors.HYPERLINK_ATTRIBUTES)
                return TextAttributes.merge(commandToRun, link)
            }

        private fun isExecutable(file: File) = if (SystemInfo.isWindows) {
            val pathExtensions = PathEnvironmentVariableUtil.getWindowsExecutableFileExtensions()
            val extensionWithDot = "." + file.extension.lowercase()
            pathExtensions.contains(extensionWithDot)
        } else {
            file.canExecute()
        }

        private fun buildHyperlinkInfo(url: String) = HyperlinkInfo {
            val file = URLUtil.urlToFile(URL(url))
            logger.info("Link to file \"$file\" was clicked")
            if (isExecutable(file)) {
                logger.info("File \"$file\" is executable, executing")
                commandExecutor.value.runProgram(file)
            } else {
                logger.info("File \"$file\" is not executable, opening")
                val virtualFile = localFileSystem.value.findFileByIoFile(file)
                if (virtualFile == null) {
                    logger.warn("Virtual file is null for \"$file\"")
                    return@HyperlinkInfo
                }

                fileEditorManager.value.openFile(virtualFile, true)
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

        private fun createResultItem(
            textStartOffset: Int,
            m: Matcher
        ) = Filter.ResultItem(
            textStartOffset + m.start(),
            textStartOffset + m.end(),
            buildHyperlinkInfo(m.group()),
            highlightAttributes
        )
    }
}
