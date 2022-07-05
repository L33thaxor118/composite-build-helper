package util

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManager
import org.jetbrains.plugins.terminal.ShellTerminalWidget
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory
import org.jetbrains.plugins.terminal.TerminalView
import org.jetbrains.plugins.terminal.arrangement.TerminalWorkingDirectoryManager
import java.io.IOException
import java.util.*

class TerminalOpener(private val project: Project) {
    // Based on this: https://github.com/JetBrains/intellij-community/blob/95ab6a1ecfdf49754f7eb5a81984cdc2c4fa0ca5/plugins/sh/src/com/intellij/sh/run/ShTerminalRunner.java
    // Looks like beyond plugin extensions, we can use classes defined in other plugins as well. In this case, we use TerminalView from intelliJs Terminal plugin.
    fun openTerminal(workingDirectory: String, tabName: String) {
        val terminalView = TerminalView.getInstance(project)
        val window = ToolWindowManager.getInstance(project).getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID) ?: return
        val contentManager = window.contentManager
        val pair = getSuitableProcess(contentManager, workingDirectory)
        try {
            if (pair == null) {
                terminalView.createLocalShellWidget(workingDirectory, tabName)
                return
            }
            window.activate(null)
            contentManager.setSelectedContent(pair.first!!)
        } catch (e: IOException) {
            // Ignore
        }
    }

    private fun getSuitableProcess(contentManager: ContentManager, workingDirectory: String): Pair<Content?, ShellTerminalWidget?>? {
        val selectedContent = contentManager.selectedContent
        if (selectedContent != null) {
            val pair = getSuitableProcess(selectedContent, workingDirectory)
            if (pair != null) return pair
        }
        return Arrays.stream(contentManager.contents)
            .map { content -> getSuitableProcess(content, workingDirectory) }
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null)
    }

    private fun getSuitableProcess(content: Content, workingDirectory: String): Pair<Content?, ShellTerminalWidget?>? {
        val widget = TerminalView.getWidgetByContent(content) as? ShellTerminalWidget ?: return null
        val currentWorkingDirectory = TerminalWorkingDirectoryManager.getWorkingDirectory(widget, null)
        return if (currentWorkingDirectory == workingDirectory) {
            Pair(content, widget)
        } else {
            null
        }
    }
}