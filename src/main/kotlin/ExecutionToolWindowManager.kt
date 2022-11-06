package me.fornever.filelinkexecutor

import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowBalloonShowOptions
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import com.intellij.util.concurrency.annotations.RequiresEdt

private const val TOOL_WINDOW_ID = ToolWindowId.RUN

@Service(Service.Level.PROJECT)
class ExecutionToolWindowManager(
    private val toolWindowManager: Lazy<ToolWindowManager>
) {

    private data class ContentTab(val process: ProcessHandler, val console: ConsoleView, val content: Content)
    private val contentTabs = mutableListOf<ContentTab>()

    @Suppress("unused")
    constructor(project: Project) : this(
        lazy { ToolWindowManager.getInstance(project) }
    )

    companion object {
        fun getInstance(project: Project): ExecutionToolWindowManager = project.service()
    }

    /**
     * Either finds the first non-pinned tab with terminated process created by this manager and replaces the content,
     * or creates a new tab in the Run tool window.
     */
    @RequiresEdt
    fun addTab(process: OSProcessHandler, console: ConsoleView, tabName: String) {
        val contentIndexToReplace = getContentToReplace()
        if (contentIndexToReplace == null) {
            val toolWindow = getToolWindow()
            val content = toolWindow.contentManager.factory.createContent(console.component, tabName, true)

            contentTabs.add(ContentTab(process, console, content))
            toolWindow.contentManager.addContent(content)
        } else {
            val (_, oldConsole, oldContent) = contentTabs[contentIndexToReplace]
            oldContent.component = console.component
            oldContent.tabName = tabName
            Disposer.dispose(oldConsole)
            contentTabs[contentIndexToReplace] = ContentTab(process, console, oldContent)
        }
    }

    @RequiresEdt
    fun notifyByBalloon(messageType: MessageType, text: String) {
        val balloon = ToolWindowBalloonShowOptions(
            TOOL_WINDOW_ID,
            messageType,
            text
        )
        toolWindowManager.value.notifyByBalloon(balloon)
    }

    private fun getToolWindow(): ToolWindow {
        val toolWindow = toolWindowManager.value.getToolWindow(TOOL_WINDOW_ID)
            ?: error("Cannot find tool window with id \"$TOOL_WINDOW_ID\"")
        toolWindow.addContentManagerListener(object : ContentManagerListener {
            @RequiresEdt
            override fun contentRemoved(event: ContentManagerEvent) {
                val content = event.content
                val index = contentTabs.indexOfFirst { (_, _, c) -> c == content }
                if (index != -1) {
                    val (_, console, _) = contentTabs.removeAt(index)
                    Disposer.dispose(console)
                }
            }
        })
        return toolWindow
    }

    private fun getContentToReplace(): Int? {
        val index = contentTabs.indexOfFirst { (process, _, content) ->
            process.isProcessTerminated && !content.isPinned
        }
        return if (index == -1) null else index
    }
}
