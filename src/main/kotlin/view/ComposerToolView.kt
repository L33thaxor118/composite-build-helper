package view

import repository.GradleBuildRepository
import com.intellij.openapi.project.Project
import kotlinx.coroutines.*
import util.IncludedBuildController
import util.TerminalOpener
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JProgressBar


class ComposerToolView(
    private val project: Project,
    private val buildRepository: GradleBuildRepository,
    private val terminalOpener: TerminalOpener,
    private val includedBuildController: IncludedBuildController
) {
    private val viewScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val component: JComponent
        get() {
            return container
        }

    private val rootDirPickerView = RootDirPickerView(project).apply {
        onSearch { path ->
            viewScope.launch {
                progressBar.isVisible = true
                val builds = buildRepository.getGradleBuilds(path)
                buildTableView.setBuilds(builds)
                progressBar.isVisible = false
            }
        }
    }

    private val controlsView = ControlsView().apply {
        onGitRefresh {
            viewScope.launch {
                progressBar.isVisible = true
                val builds = buildTableView.getBuilds()
                builds.forEach { build ->
                    build.repoStatus?.let { _ ->
                        build.repoStatus = buildRepository.getRepoStatusForBuild(build)
                    }
                }
                buildTableView.setBuilds(builds)
                progressBar.isVisible = false
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