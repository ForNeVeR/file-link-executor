package me.fornever.filelinkexecutor

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.withBackgroundProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.util.PathUtil
import com.intellij.util.application
import kotlinx.coroutines.*
import java.io.File

@Service(Service.Level.PROJECT)
class CommandExecutor(
    private val project: Project,
    private val processHandlerFactory: Lazy<ProcessHandlerFactory>,
    private val executionToolWindowManager: Lazy<ExecutionToolWindowManager>,
    private val textConsoleBuilderFactory: Lazy<TextConsoleBuilderFactory>
) : Disposable {

    @Suppress("unused")
    constructor(project: Project) : this(
        project,
        lazy { ProcessHandlerFactory.getInstance() },
        lazy { ExecutionToolWindowManager.getInstance(project) },
        lazy { TextConsoleBuilderFactory.getInstance() }
    )

    companion object {
        fun getInstance(project: Project): CommandExecutor = project.service()
    }

    private val scope = CoroutineScope(Dispatchers.EDT)
    override fun dispose() {
        scope.cancel()
    }

    fun runProgram(program: File) {
        val cmd = GeneralCommandLine(PathUtil.toSystemIndependentName(program.absolutePath))
        val programName = program.name

        val console = textConsoleBuilderFactory.value.createBuilder(project).console
        val (processHandler, descriptor) = startProcess(cmd, programName, console)
        val listener = attachExecutionListener(processHandler, programName, console, descriptor)
        startProgressIndicator(listener, programName)

        console.attachToProcess(processHandler)
        processHandler.startNotify()
    }

    private data class RunResult(val processHandler: ProcessHandler, val contentDescriptor: RunContentDescriptor)
    private fun startProcess(command: GeneralCommandLine, programName: String, console: ConsoleView): RunResult {
        val processHandler = processHandlerFactory.value.createProcessHandler(command)
        val descriptor = executionToolWindowManager.value.addTab(
            processHandler,
            console,
            programName
        )

        return RunResult(processHandler, descriptor)
    }

    private fun attachExecutionListener(
        processHandler: ProcessHandler,
        programName: String,
        console: ConsoleView,
        descriptor: RunContentDescriptor
    ) = ExecutionListener(executionToolWindowManager, programName, console, descriptor).apply {
        processHandler.addProcessListener(this)
    }

    private fun startProgressIndicator(listener: ExecutionListener, programName: String) {
        scope.launch {
            @Suppress("UnstableApiUsage")
            withBackgroundProgressIndicator(
                project,
                FileLinkExecutorBundle.message("execution.running", programName)
            ) {
                listener.termination.await()
            }
        }
    }
}

private class ExecutionListener(
    private val executionToolWindowManager: Lazy<ExecutionToolWindowManager>,
    private val programName: String,
    private val console: ConsoleView,
    private val descriptor: RunContentDescriptor
) : ProcessAdapter() {

    private val _termination = CompletableDeferred<Unit>()
    val termination: Deferred<Unit>
        get() = _termination

    override fun processTerminated(event: ProcessEvent) {
        _termination.complete(Unit)
        notifyCompletion(event)
    }

    private fun notifyCompletion(event: ProcessEvent) {
        val success = event.exitCode == 0
        application.invokeLater {
            console.print(
                FileLinkExecutorBundle.message("execution.terminatedWithCode", event.exitCode),
                ConsoleViewContentType.SYSTEM_OUTPUT
            )
            executionToolWindowManager.value.notifyByBalloon(
                descriptor,
                if (success) MessageType.INFO else MessageType.ERROR,
                if (success)
                    FileLinkExecutorBundle.message("execution.success", programName)
                else
                    FileLinkExecutorBundle.message("execution.failed", programName)
            )
        }
    }
}
