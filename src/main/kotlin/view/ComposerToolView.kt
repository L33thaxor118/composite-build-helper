package view

import repository.GradleBuildRepository
import com.intellij.openapi.project.Project
import kotlinx.coroutines.*
import util.IncludedBuildController
import util.TerminalOpener
import javax.swing.*


class ComposerToolView(
    private val project: Project,
    private val buildRepository: GradleBuildRepository,
    private val terminalOpener: TerminalOpener,
    private val includedBuildController: IncludedBuildController
) {
    // I would like to use Dispatchers.Main here but I'm having issues with IntelliJ.
    // https://youtrack.jetbrains.com/issue/IDEA-285792/Implement-DispatchersMain
    private val viewScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val component: JComponent
        get() {
            return container
        }

    private val rootDirPickerView = RootDirPickerView(project).apply {
        onSearch { path ->
            progressBar.isVisible = true
            viewScope.launch {
                val builds = buildRepository.getGradleBuilds(path)
                SwingUtilities.invokeLater(Runnable {
                    buildTableView.setBuilds(builds)
                    progressBar.isVisible = false
                })
            }
        }
    }

    private val controlsView = ControlsView().apply {
        onGitRefresh {
            progressBar.isVisible = true
            viewScope.launch {
                val builds = buildTableView.getBuilds()
                builds.forEach { build ->
                    build.repoStatus?.let { _ ->
                        build.repoStatus = buildRepository.getRepoStatusForBuild(build)
                    }
                }
                SwingUtilities.invokeLater(Runnable {
                    buildTableView.setBuilds(builds)
                    progressBar.isVisible = false
                })
            }
        }
        onUpdateSettings {
            includedBuildController.updateBuildInclusion(buildTableView.getBuilds().map { it.inclusionSettings })
        }
    }

    private val progressBar = JProgressBar().apply {
        isIndeterminate = true
        isVisible = false
    }

    private val buildTableView = BuildTableView().apply {
        onTerminalOpenRequest {
            terminalOpener.openTerminal(it.path, it.rootProjectName ?: "")
        }
        onBuildInclusionChange { build, include ->
            buildRepository.updateBuildInclusionSettings(build)
        }
        onBuildSubstitutionChange { build, substitute ->
            buildRepository.updateBuildInclusionSettings(build)
        }
        onBuildUsageChange { build, using ->
            buildRepository.updateBuildInclusionSettings(build)
        }
    }

    private val container = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        add(rootDirPickerView.component)
        add(controlsView.component)
        add(progressBar)
        add(buildTableView.component)
    }
}