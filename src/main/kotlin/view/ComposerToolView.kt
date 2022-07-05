package view

import GradleBuildRepository
import com.intellij.openapi.project.Project
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import util.IncludedBuildController
import util.TerminalOpener
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel


class ComposerToolView(
    private val project: Project,
    private val buildRepository: GradleBuildRepository,
    private val terminalOpener: TerminalOpener,
    private val includedBuildController: IncludedBuildController
) {
    val component: JComponent
        get() {
            return container
        }

    private val rootDirPickerView = RootDirPickerView(project).apply {
        onSearch { path ->
            GlobalScope.launch {
                val builds = buildRepository.getGradleBuilds(path)
                buildTableView.setBuilds(builds)
            }
        }
    }

    private val controlsView = ControlsView().apply {
        onGitRefresh {
            GlobalScope.launch {
                val builds = buildTableView.getBuilds()
                builds.forEach { build ->
                    build.repoStatus?.let { _ ->
                        build.repoStatus = buildRepository.getRepoStatusForBuild(build)
                    }
                }
                buildTableView.setBuilds(builds)
            }
        }
        onUpdateSettings {
            includedBuildController.updateBuildInclusion(buildTableView.getBuilds().map { it.inclusionSettings })
        }
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
        add(buildTableView.component)
    }
}