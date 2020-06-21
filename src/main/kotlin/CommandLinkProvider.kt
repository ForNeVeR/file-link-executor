package me.fornever.commandlink

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.PathUtil
import com.intellij.util.io.URLUtil
import java.io.File
import java.net.URL
import java.util.*
import java.util.function.Supplier

class CommandLinkProvider : ConsoleFilterProvider {

    companion object {
        private const val TOOL_WINDOW_ID = "me.fornever.filelinkexecutor.Commands"
        private val logger = Logger.getInstance(CommandLinkProvider::class.java)
    }

    private fun getToolWindow(project: Project): ToolWindow {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        return toolWindowManager.getToolWindow(TOOL_WINDOW_ID)
            ?: toolWindowManager.registerToolWindow(RegisterToolWindowTask(
                TOOL_WINDOW_ID,
                stripeTitle = Supplier { "Commands" }
            ))
    }

    override fun getDefaultFilters(project: Project): Array<Filter> {
        return arrayOf(CommandLinkFilter(project))
    }

    inner class CommandLinkFilter(private val project: Project) : Filter {

        private fun runProgram(project: Project, program: File) {
            val cmd = GeneralCommandLine(PathUtil.toSystemIndependentName(program.absolutePath))
            val processHandlerFactory = ProcessHandlerFactory.getInstance()
            val processHandler = processHandlerFactory.createProcessHandler(cmd)

            val toolWindow = getToolWindow(project)
            val consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
            val content = toolWindow.contentManager.factory.createContent(consoleView.component, program.name, true)
            toolWindow.contentManager.addContent(content)

            processHandler.startNotify()
            consoleView.attachToProcess(processHandler)
        }

        private fun isExecutable(file: File) = if (SystemInfo.isWindows) {
            val pathExtensions = PathEnvironmentVariableUtil.getWindowsExecutableFileExtensions()
            val extensionWithDot = "." + file.extension.toLowerCase(Locale.ROOT)
            pathExtensions.contains(extensionWithDot)
        } else {
            file.canExecute()
        }

        private fun buildHyperlinkInfo(url: String) = HyperlinkInfo {
            val file = URLUtil.urlToFile(URL(url))
            logger.info("Link to file \"$file\" was clicked")
            if (isExecutable(file)) {
                logger.info("File \"$file\" is executable, executing")
                runProgram(project, file)
            } else {
                logger.info("File \"$file\" is not executable, opening")
                val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file)
                if (virtualFile == null) {
                    logger.warn("Virtual file is null for \"$file\"")
                    return@HyperlinkInfo
                }

                FileEditorManager.getInstance(project).openFile(virtualFile, true)
            }
        }

        override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
            if (!URLUtil.canContainUrl(line) || !line.contains(URLUtil.FILE_PROTOCOL + URLUtil.SCHEME_SEPARATOR)) return null

            val pattern = URLUtil.FILE_URL_PATTERN
            val m = pattern.matcher(line)
            var item: Filter.ResultItem? = null
            var items: MutableList<Filter.ResultItem>? = null
            val textStartOffset = entireLength - line.length
            while (m.find()) {
                if (item == null) {
                    item = Filter.ResultItem(textStartOffset + m.start(), textStartOffset + m.end(), buildHyperlinkInfo(m.group()))
                } else {
                    if (items == null) {
                        items = ArrayList(2)
                        items.add(item)
                    }
                    items.add(Filter.ResultItem(textStartOffset + m.start(), textStartOffset + m.end(), buildHyperlinkInfo(m.group())))
                }
            }

            return when {
                items != null -> Filter.Result(items)
                item != null -> Filter.Result(item.highlightStartOffset, item.highlightEndOffset, item.getHyperlinkInfo())
                else -> null
            }
        }
    }
}
