
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import dao.GitRepoStatusDao
import dao.GradleBuildDao
import dao.BuildInclusionSettingsDao
import util.IncludedBuildController
import util.TerminalOpener
import view.ComposerToolView

class ComposerToolWindowFactory: ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val buildDao = GradleBuildDao()
        val repoDao = GitRepoStatusDao()
        val terminalOpener = TerminalOpener(project)
        val settingsDao = BuildInclusionSettingsDao(project)
        val buildRepository = GradleBuildRepository(project, buildDao, repoDao, settingsDao)
        val includedBuildController = IncludedBuildController(project)
        toolWindow.contentManager.apply {
            addContent(
                factory.createContent(
                    ComposerToolView(
                        project,
                        buildRepository,
                        terminalOpener,
                        includedBuildController,
                    ).component, "Welcome", false)
            )
        }
    }
}