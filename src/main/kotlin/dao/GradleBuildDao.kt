package dao

import model.GradleBuild
import java.io.File
import java.util.*
import java.util.regex.Pattern

/**
 * Performs read operations to get existing GradleBuilds from a directory.
 *
 */
class GradleBuildDao {

    fun getBuilds(inPath: String): List<GradleBuild> {
        val projectDirs = File(inPath)
            .walk()
            .filter { it.isDirectory }
            .filter { File(it.path, "settings.gradle").exists() }
            .toList()
        val builds = projectDirs.map { GradleBuild(it.path) }
        builds.forEach { it.rootProjectName = getRootProjectName(it) }
        return builds
    }

    private fun getRootProjectName(project: GradleBuild): String {
        var name = ""
        try {
            val scanner = Scanner(File(project.path, "settings.gradle"))
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
}