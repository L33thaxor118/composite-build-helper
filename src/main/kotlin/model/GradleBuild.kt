package model

/**
 * Representation of a Gradle build which can be included in a composite build
 */
class GradleBuild(val path: String) {
    var rootProjectName: String = ""

    // Version data for this instance of Gradle build
    var repoStatus: GitRepoStatus? = null

    // Settings describing how (and if) this build should be included in the current composite build
    var inclusionSettings: BuildInclusionSettings = BuildInclusionSettings(path, null, null, false)
}
