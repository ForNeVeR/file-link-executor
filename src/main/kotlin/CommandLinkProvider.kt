package me.fornever.commandlink

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.PathUtil
import com.intellij.util.io.URLUtil
import java.io.File
import java.net.URL
import java.util.*

class CommandLinkProvider : ConsoleFilterProvider {

    private var toolWindow: ToolWindow? = null
    private fun initToolWindow(project: Project): ToolWindow {
        if (toolWindow == null) {
            toolWindow = ToolWindowManager.getInstance(project).registerToolWindow("Commands", true, ToolWindowAnchor.BOTTOM)
        }

        return toolWindow!!
    }

    override fun getDefaultFilters(project: Project): Array<Filter> {
        return arrayOf(CommandLinkFilter(project))
    }

    inner class CommandLinkFilter(private val project: Project) : Filter {

        private fun runProgram(project: Project, program: File) {
            val cmd = GeneralCommandLine(PathUtil.toSystemIndependentName(program.absolutePath))
            val processHandlerFactory = ProcessHandlerFactory.getInstance()
            val processHandler = processHandlerFactory.createProcessHandler(cmd)

            val toolWindow = initToolWindow(project)
            val consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
            val content = toolWindow.contentManager.factory.createContent(consoleView.component, program.name, true)
            toolWindow.contentManager.addContent(content)

            processHandler.startNotify()
            consoleView.attachToProcess(processHandler)
        }

        private fun buildHyperlinkInfo(url: String) = HyperlinkInfo {
            runProgram(project, URLUtil.urlToFile(URL(url)))
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
                item != null -> Filter.Result(item.getHighlightStartOffset(), item.getHighlightEndOffset(), item.getHyperlinkInfo())
                else -> null
            }
        }
    }
}
