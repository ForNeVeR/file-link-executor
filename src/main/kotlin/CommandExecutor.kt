package me.fornever.filelinkexecutor

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowBalloonShowOptions
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.PathUtil
import com.intellij.util.application
import java.io.File

@Service(Service.Level.PROJECT)
class CommandExecutor(
    private val project: Project,
    private val processHandlerFactory: Lazy<ProcessHandlerFactory>,
    private val toolWindowManager: Lazy<ToolWindowManager>,
    private val textConsoleBuilderFactory: Lazy<TextConsoleBuilderFactory>
) {

    @Suppress("unused")
    constructor(project: Project) : this(
        project,
        lazy { ProcessHandlerFactory.getInstance() },
        lazy { ToolWindowManager.getInstance(project) },
        lazy { TextConsoleBuilderFactory.getInstance() }
    )

    companion object {
        fun getInstance(project: Project): CommandExecutor = project.service()

        private const val TOOL_WINDOW_ID = "me.fornever.filelinkexecutor.Commands"
    }

    fun runProgram(program: File) {
        val cmd = GeneralCommandLine(PathUtil.toSystemIndependentName(program.absolutePath))
        val processHandler = processHandlerFactory.value.createProcessHandler(cmd).apply {
            attachExecutionListener(this, program.name)
        }

        val toolWindow = getToolWindow()
        val consoleView = textConsoleBuilderFactory.value.createBuilder(project).console
        val content = toolWindow.contentManager.factory.createContent(consoleView.component, program.name, true)
        toolWindow.contentManager.addContent(content)

        consoleView.attachToProcess(processHandler)
        processHandler.startNotify()
    }

    private fun getToolWindow(): ToolWindow {
        return toolWindowManager.value.getToolWindow(TOOL_WINDOW_ID)
            ?: toolWindowManager.value.registerToolWindow(RegisterToolWindowTask(
                TOOL_WINDOW_ID,
                stripeTitle = { "Commands" }
            ))
    }

    private fun attachExecutionListener(processHandler: ProcessHandler, name: String) {
        processHandler.addProcessListener(object : ProcessAdapter() {
            override fun processTerminated(event: ProcessEvent) {
                val success = event.exitCode == 0
                val balloon = ToolWindowBalloonShowOptions(
                    TOOL_WINDOW_ID,
                    if (success) MessageType.INFO else MessageType.ERROR,
                    if (success)
                        FileLinkExecutorBundle.message("execution.success", name)
                    else
                        FileLinkExecutorBundle.message("execution.failed", name)
                )
                application.invokeLater {
                    toolWindowManager.value.notifyByBalloon(balloon)
                }
            }
        })
    }
}
