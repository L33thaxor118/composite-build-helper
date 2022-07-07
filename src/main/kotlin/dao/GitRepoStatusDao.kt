package dao

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.GitRepoStatus
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


class GitRepoStatusDao {

    suspend fun getRepoStatus(repoPath: String): GitRepoStatus? = withContext(Dispatchers.IO) {
        val directory = File(repoPath)
        val gitFile = File(repoPath, ".git")
        if (!directory.isDirectory || !gitFile.exists()) {
            return@withContext null
        }
        val currentTag = getCurrentTag(directory)
        val isWorkingTreeClean = isWorkingTreeClean(directory)
        return@withContext GitRepoStatus(repoPath, currentTag, isWorkingTreeClean)
    }

    private suspend fun getCurrentTag(repoDir: File): String? = withContext(Dispatchers.IO) {
        return@withContext try {
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

    private suspend fun isWorkingTreeClean(repoDir: File): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val pb = ProcessBuilder()
                .command("git", "status", "--short")
                .directory(repoDir)
            val pr = pb.start()
            pr.waitFor()
            val output = BufferedReader(InputStreamReader(pr.inputStream))
            output.readLine() ?: return@withContext true
            return@withContext false
        } catch (e: Exception) { true }
    }
}