import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


class GitRepoController(val repoPath: String) {
    private val isGitRepo: Boolean

    init {
        val file = File(repoPath)
        isGitRepo = if (file.isDirectory) {
            File(file.path, ".git").exists()
        } else {
            false
        }
    }

    /**
     * Gets tag for currently checked-out commit, if any
     */
    fun getCurrentTag(): String? {
        if (!isGitRepo) {
            return null
        }
        val pb = ProcessBuilder()
            .command("git", "describe", "--tags")
            .directory(File(repoPath))
        val pr = pb.start()
        pr.waitFor()
        val output = BufferedReader(InputStreamReader(pr.inputStream))
        val value = output.readLine()
        return value?.let {
            if (value.contains("fatal")) null else value
        }
    }

    fun isWorkingTreeClean(): Boolean {
        if (!isGitRepo) {
            return true
        }
        val pb = ProcessBuilder()
            .command("git", "status", "--short")
            .directory(File(repoPath))
        val pr = pb.start()
        pr.waitFor()
        val output = BufferedReader(InputStreamReader(pr.inputStream))
        output.readLine() ?: return true
        return false
    }
}