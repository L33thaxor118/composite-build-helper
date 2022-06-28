import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File
import java.lang.StringBuilder
import java.util.*

class ProjectDependencyManager(private val project: Project) {

    enum class DependencyAction { ADD, REMOVE, UPDATE }
    fun updateProjectBuildInclusion(dependency: ProjectDependency, include: Boolean) {
        if (include) {
            updateDependencies(dependency, DependencyAction.ADD)
        } else {
            updateDependencies(dependency, DependencyAction.REMOVE)
        }
    }

    fun isDependencyIncluded(dependency: ProjectDependency): Boolean {
        var dependencyFound = false
        LocalFileSystem.getInstance().findFileByIoFile(File(project.basePath, "settings.gradle"))?.let {
            val document = FileDocumentManager.getInstance().getDocument(it)
            val existingContentScanner = Scanner(document?.text)
            while(existingContentScanner.hasNextLine()) {
                val line = existingContentScanner.nextLine()
                if (line.contains(dependency.path)) {
                    dependencyFound = true
                }
            }
            existingContentScanner.close()
        }
        return dependencyFound
    }

    private fun updateDependencies(dependency: ProjectDependency, action: DependencyAction) {
        LocalFileSystem.getInstance().findFileByIoFile(File(project.basePath, "settings.gradle"))?.let {
            val document = FileDocumentManager.getInstance().getDocument(it)
            try {
                val newSettings = when(action) {
                    DependencyAction.ADD -> getSettingsWithAddedDependency(dependency, document!!)
                    DependencyAction.REMOVE -> getSettingsWithRemovedDependency(dependency, document!!)
                    DependencyAction.UPDATE -> getSettingsWithUpdatedDependency(dependency, document!!)
                }
                val r = Runnable {
                    document!!.setReadOnly(false)
                    document.setText(newSettings)
                }
                WriteCommandAction.runWriteCommandAction(project, r)
            } catch (ignore: Exception) {
                // Do nothing
            }
        }
    }

    private fun getSettingsWithAddedDependency(dependency: ProjectDependency, settings: Document): String {
        val existingContentScanner = Scanner(settings.text)
        val newContentBuilder = StringBuilder()
        while(existingContentScanner.hasNextLine()) {
            val line = existingContentScanner.nextLine()
            newContentBuilder.appendLine(line)
        }
        newContentBuilder.appendLine(dependency.inclusionStatement)
        existingContentScanner.close()
        return newContentBuilder.toString()
    }

    private fun getSettingsWithRemovedDependency(dependency: ProjectDependency, settings: Document): String {
        val existingContentScanner = Scanner(settings.text)
        val newContentBuilder = StringBuilder()
        while(existingContentScanner.hasNextLine()) {
            val line = existingContentScanner.nextLine()
            if (!line.contains(dependency.path)) {
                newContentBuilder.appendLine(line)
            }
        }
        existingContentScanner.close()
        return newContentBuilder.toString()
    }

    private fun getSettingsWithUpdatedDependency(dependency: ProjectDependency, settings: Document): String {
        //TODO: implement
        return ""
    }
}