import org.eclipse.jgit.revwalk.DepthWalk
import java.io.File
import java.util.*
import java.util.regex.Pattern

class ProjectItem(val name: String, val path: String) {

    var includeBuild: Boolean = false
    val checkedOutVersion: String = "?"

    companion object {
        fun from(dir: File): ProjectItem? {
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
            } catch(e: Exception) {
                return null
            }
            return ProjectItem(name, dir.path)
        }
    }

    init {

    }
}
