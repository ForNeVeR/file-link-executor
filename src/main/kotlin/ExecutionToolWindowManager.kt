package me.fornever.filelinkexecutor

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowBalloonShowOptions
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.concurrency.annotations.RequiresEdt
import javax.swing.JComponent

private const val TOOL_WINDOW_ID = "me.fornever.filelinkexecutor.Commands"

@Service(Service.Level.PROJECT)
class ExecutionToolWindowManager(
    private val toolWindowManager: Lazy<ToolWindowManager>
) {

    @Suppress("unused")
    constructor(project: Project) : this(
        lazy { ToolWindowManager.getInstance(project) }
    )

    companion object {
        fun getInstance(project: Project): ExecutionToolWindowManager = project.service()
    }

    @RequiresEdt
    fun addTab(component: JComponent, tabName: String) {
        val toolWindow = getToolWindow()
        val content = toolWindow.contentManager.factory.createContent(component, tabName, true)
        toolWindow.contentManager.addContent(content)
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
        return toolWindowManager.value.getToolWindow(TOOL_WINDOW_ID)
            ?: toolWindowManager.value.registerToolWindow(RegisterToolWindowTask(
                TOOL_WINDOW_ID,
                stripeTitle = { "Commands" }
            ))
    }
}
