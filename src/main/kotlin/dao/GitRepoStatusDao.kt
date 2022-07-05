package dao

import model.GitRepoStatus
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


class GitRepoStatusDao {

    fun getRepoStatus(repoPath: String): GitRepoStatus? {
        val directory = File(repoPath)
        val gitFile = File(repoPath, ".git")
        if (!directory.isDirectory || !gitFile.exists()) {
            return null
        }
        val currentTag = getCurrentTag(directory)
        val isWorkingTreeClean = isWorkingTreeClean(directory)
        return GitRepoStatus(repoPath, currentTag, isWorkingTreeClean)
    }

    private fun getCurrentTag(repoDir: File): String? {
        return try {
            val pb = ProcessBuilder()
                .command("git", "describe", "--tags")
                .directory(repoDir)
            val pr = pb.start()
            pr.waitFor()
            val output = BufferedReader(InputStreamReader(pr.inputStream))
            val value = output.readLine()
            value?.let {
                if (value.contains("fatal")) null else value
            }
        } catch (e: Exception) { null }
    }

    private fun isWorkingTreeClean(repoDir: File): Boolean {
        return try {
            val pb = ProcessBuilder()
                .command("git", "status", "--short")
                .directory(repoDir)
            val pr = pb.start()
            pr.waitFor()
            val output = BufferedReader(InputStreamReader(pr.inputStream))
            output.readLine() ?: return true
            return false
        } catch (e: Exception) { true }
    }
}