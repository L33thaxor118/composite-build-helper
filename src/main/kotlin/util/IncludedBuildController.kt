package util

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import model.BuildInclusionSettings
import java.io.File
import java.util.*
import java.util.regex.Pattern

class IncludedBuildController(val project: Project) {
    private val settingsFile = File(project.basePath, "settings.gradle")
    private val detailedBuildInclusionRegex = "includeBuild\\('(.*)'\\) \\{dependencySubstitution \\{substitute module\\('(.*)'\\) using project\\(':(.*)'\\)\\}\\}"
    private val simpleBuildInclusionRegex = "includeBuild '(.*)'"

    /**
     * Updates settings.gradle file to reflect provided build inclusion settings
     */
    fun updateBuildInclusion(updatedSettings: List<BuildInclusionSettings>) {
        LocalFileSystem.getInstance().findFileByIoFile(settingsFile)?.let { virtualFile ->
            val document = FileDocumentManager.getInstance().getDocument(virtualFile) ?: return
            val existingContent = Scanner(document.text)
            val newContent = StringBuilder()

            val paths = updatedSettings.map { it.path }.toSet()
            val includedPaths = mutableSetOf<String>()

            while(existingContent.hasNextLine()) {
                val currentLine = existingContent.nextLine()
                var replacementLine: String? = currentLine
                parseProjectDependencyInclusion(currentLine)?.let { existingSetting ->
                    if (paths.contains(existingSetting.path)) {
                        val updatedSetting = updatedSettings.find { it.path == existingSetting.path }!!
                        replacementLine = if (updatedSetting.include) {
                            includedPaths.add(existingSetting.path)
                            updatedSetting.inclusionStatement
                        } else {
                            null
                        }
                    }
                }
                replacementLine?.let {
                    newContent.appendLine(it)
                }
            }
            updatedSettings
                .filter { it.include }
                .filter { !includedPaths.contains(it.path) }
                .forEach { newContent.appendLine(it.inclusionStatement) }

            existingContent.close()
            val r = Runnable {
                document.setReadOnly(false)
                document.setText(newContent.toString())
            }
            WriteCommandAction.runWriteCommandAction(project, r)
        }
    }

    private fun parseProjectDependencyInclusion(line: String): BuildInclusionSettings? {
        val detailMatcher = Pattern.compile(detailedBuildInclusionRegex).matcher(line)
        val simpleMatcher = Pattern.compile(simpleBuildInclusionRegex).matcher(line)
        return if (detailMatcher.find()) {
            val path = detailMatcher.group(1)
            val substitute = detailMatcher.group(2)
            val using = detailMatcher.group(3)
            BuildInclusionSettings(path, substitute, using, true)
        } else if (simpleMatcher.find()) {
            val path = simpleMatcher.group(1)
            BuildInclusionSettings(path, null, null, true)
        } else {
            null
        }
    }
}