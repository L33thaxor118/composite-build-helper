
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class ComposerToolWindowFactory: ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.apply {
            title = "Hello!"
            val tool = ComposerTool(project)
            val content = contentManager.factory.createContent(tool.getView(), "Welcome", false)
            contentManager.addContent(content)
        }
    }
}