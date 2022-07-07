import com.intellij.openapi.project.Project
import dao.GitRepoStatusDao
import dao.GradleBuildDao
import dao.BuildInclusionSettingsDao
import model.GitRepoStatus
import model.GradleBuild
import java.lang.IllegalArgumentException

class GradleBuildRepository(
    val project: Project,
    val gradleBuildDao: GradleBuildDao,
    val gitRepositoryDao: GitRepoStatusDao,
    val buildInclusionSettingsDao: BuildInclusionSettingsDao) {

    suspend fun getGradleBuilds(path: String): List<GradleBuild> {
        val builds = gradleBuildDao.getBuilds(path)
        builds.forEach { it.repoStatus = gitRepositoryDao.getRepoStatus(it.path) }
        builds.forEach { it.inclusionSettings = buildInclusionSettingsDao.getBuildInclusionSettings(it.path) }
        return builds
    }

    suspend fun getRepoStatusForBuild(build: GradleBuild): GitRepoStatus {
        return gitRepositoryDao.getRepoStatus(build.path) ?: throw IllegalArgumentException()
    }

    fun updateBuildInclusionSettings(build: GradleBuild) {
        buildInclusionSettingsDao.updateBuildInclusionSettings(build.path, build.inclusionSettings)
    }
}