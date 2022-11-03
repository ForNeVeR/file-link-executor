package me.fornever.commandlink

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.PathUtil
import java.io.File

object ProcessUtils {
    private const val TOOL_WINDOW_ID = "me.fornever.filelinkexecutor.Commands"

    fun runProgram(project: Project, program: File) {
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

    private fun getToolWindow(project: Project): ToolWindow {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        return toolWindowManager.getToolWindow(TOOL_WINDOW_ID)
            ?: toolWindowManager.registerToolWindow(RegisterToolWindowTask(
                TOOL_WINDOW_ID,
                stripeTitle = { "Commands" }
            ))
    }
}
