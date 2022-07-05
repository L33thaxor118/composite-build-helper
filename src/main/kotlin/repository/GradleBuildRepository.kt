import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import dao.GitRepoStatusDao
import dao.GradleBuildDao
import dao.BuildInclusionSettingsDao
import model.GitRepoStatus
import model.GradleBuild
import java.lang.IllegalArgumentException
import kotlin.coroutines.suspendCoroutine

class GradleBuildRepository(
    val project: Project,
    val gradleBuildDao: GradleBuildDao,
    val gitRepositoryDao: GitRepoStatusDao,
    val buildInclusionSettingsDao: BuildInclusionSettingsDao) {

    suspend fun getGradleBuilds(path: String): List<GradleBuild> {
        return suspendCoroutine { cont ->
            object : Task.Backgroundable(project, "Reading builds", false) {
                var builds = listOf<GradleBuild>()
                override fun run(indicator: ProgressIndicator) {
                    indicator.text = "Reading builds"
                    indicator.isIndeterminate = false
                    indicator.fraction = 0.0
                    val builds = gradleBuildDao.getBuilds(path)
                    indicator.fraction = 0.3
                    builds.forEach { it.repoStatus = gitRepositoryDao.getRepoStatus(it.path) }
                    indicator.fraction = 0.6
                    builds.forEach { it.inclusionSettings = buildInclusionSettingsDao.getBuildInclusionSettings(it.path) }
                    this.builds = builds
                    indicator.fraction = 1.0
                }

                override fun onSuccess() {
                    super.onSuccess()
                    cont.resumeWith(Result.success(builds))
                }
            }.queue()
        }
    }

    suspend fun getRepoStatusForBuild(build: GradleBuild): GitRepoStatus {
        return suspendCoroutine { cont ->
            val result = gitRepositoryDao.getRepoStatus(build.path)
            result?.let {
                cont.resumeWith(Result.success(it))
            } ?: cont.resumeWith(Result.failure(IllegalArgumentException()))
        }
    }

    fun updateBuildInclusionSettings(build: GradleBuild) {
        buildInclusionSettingsDao.updateBuildInclusionSettings(build.path, build.inclusionSettings)
    }
}