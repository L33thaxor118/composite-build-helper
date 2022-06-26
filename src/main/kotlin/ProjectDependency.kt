import java.io.File
import java.util.*
import java.util.regex.Pattern
import kotlin.properties.Delegates

class ProjectDependency(dir: File) {
    val path: String
    val name: String
    var substitute: String
    var using: String
    var includeBuild: Boolean
    var checkedOutVersion: String
    var isClean: Boolean
    val isGitRepo: Boolean
    val repoController = GitRepoController(dir.path)

    val inclusionStatement: String
        get() = "includeBuild(\"$path\") {dependencySubstitution {substitute module('$substitute') using project('$using')}}"

    init {
        path = dir.path
        name = getProjectName(dir)
        substitute = Defaults.substitution[name] ?: ""
        using = Defaults.using[name] ?: ""
        includeBuild = false
        checkedOutVersion = getCurrentCheckedOutVersion()
        isClean = isRepoClean()
        isGitRepo = checkIsGitRepo()
    }

    private fun getProjectName(dir: File): String {
        var name = ""
        try {
            val scanner = Scanner(File(dir.path, "settings.gradle"))
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                if(line.contains("rootProject.name")) {
                    val r = Pattern.compile("\".*\"")
                    val m = r.matcher(line)
                    if (m.find()) {
                        val match = m.group(0)
                        name = match.substring(1, match.length - 1);
                    }
                }
            }
        } catch(ignored: Exception) { }
        return name
    }

    private fun getCurrentCheckedOutVersion(): String {
        return repoController.getCurrentTag() ?: "?"
    }

    private fun isRepoClean(): Boolean {
        return repoController.isWorkingTreeClean()
    }

    private fun checkIsGitRepo(): Boolean {
        return true
    }
}
