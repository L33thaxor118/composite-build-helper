package dao

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import model.BuildInclusionSettings
import model.GradleBuild
import java.io.File
import java.util.*
import java.util.regex.Pattern

class BuildInclusionSettingsDao(project: Project) {
    private val properties = PropertiesComponent.getInstance(project)

    companion object {
        const val PROPERTIES_BUILD_INCLUSION_PREFIX = "com.az.compositehelper"
    }

    fun getBuildInclusionSettings(path: String): BuildInclusionSettings {
        val include = properties.getBoolean("$PROPERTIES_BUILD_INCLUSION_PREFIX.$path.include", false)
        val substitute = properties.getValue("$PROPERTIES_BUILD_INCLUSION_PREFIX.$path.substitute", "")
        val using = properties.getValue("$PROPERTIES_BUILD_INCLUSION_PREFIX.$path.using", "")
        return BuildInclusionSettings(path, substitute, using, include)
    }

    fun updateBuildInclusionSettings(path: String, settings: BuildInclusionSettings) {
        properties.setValue("$PROPERTIES_BUILD_INCLUSION_PREFIX.$path.include", settings.include)
        properties.setValue("$PROPERTIES_BUILD_INCLUSION_PREFIX.$path.substitute", settings.substitute)
        properties.setValue("$PROPERTIES_BUILD_INCLUSION_PREFIX.$path.using", settings.using)
    }
}