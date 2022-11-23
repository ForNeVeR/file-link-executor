package me.fornever.filelinkexecutor

import com.intellij.execution.Executor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowBalloonShowOptions
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.concurrency.annotations.RequiresEdt

@Service(Service.Level.PROJECT)
class ExecutionToolWindowManager(
    private val runContentManager: Lazy<RunContentManager>,
    private val executor: Lazy<Executor>,
    private val toolWindowManager: Lazy<ToolWindowManager>
) : Disposable {

    @Suppress("unused")
    constructor(project: Project) : this(
        lazy { RunContentManager.getInstance(project) },
        lazy { DefaultRunExecutor.getRunExecutorInstance() },
        lazy { ToolWindowManager.getInstance(project) }
    )

    override fun dispose() {}

    companion object {
        fun getInstance(project: Project): ExecutionToolWindowManager = project.service()
    }

    /**
     * Either finds the first non-pinned tab with terminated process created by this manager and replaces the content,
     * or creates a new tab in the Run tool window.
     */
    @RequiresEdt
    fun addTab(process: OSProcessHandler, console: ExecutionConsole, tabName: String): RunContentDescriptor {
        val contentDescriptor = RunContentDescriptor(console, process, console.component, tabName)

        runContentManager.value.showRunContent(
            executor.value,
            contentDescriptor
        )

        return contentDescriptor
    }

    @RequiresEdt
    fun notifyByBalloon(descriptor: RunContentDescriptor, messageType: MessageType, text: String) {
        val toolWindow = runContentManager.value.getToolWindowByDescriptor(descriptor) ?: return
        val balloon = ToolWindowBalloonShowOptions(
            toolWindow.id,
            messageType,
            text
        )
        toolWindowManager.value.notifyByBalloon(balloon)
    }
}
