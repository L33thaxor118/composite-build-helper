import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


class GitRepoController(repoPath: String) {

    fun getAvailableTags(): List<String> {
        val pb = ProcessBuilder()
            .command("git", "status")
            .directory(File("/Users/azrielalvarado/AndroidStudioProjects/PluginTestApp"))
        val pr = pb.start()
        pr.waitFor()
        val output = BufferedReader(InputStreamReader(pr.inputStream))
        val value = output.readLine()
        return listOf(value)
    }

    /**
     * Gets tag for currently checked-out commit, if any
     */
    fun getCurrentTag(): String? {
        return "1.0.5"
    }

    /**
     * Checks out a tag if a tag with that name exists. Returns true if the operation succeeded
     */
    fun checkoutTag(tagName: String, force: Boolean): Boolean {
        return true
    }

    fun isWorkingTreeClean(): Boolean {
        return true
    }
}