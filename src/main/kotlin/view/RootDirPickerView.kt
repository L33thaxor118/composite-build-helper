package view

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class RootDirPickerView(private val project: Project) {
    companion object {
        private const val PROPERTIES_ROOT_MODULE_DIR = "com.az.compositehelper.moduledir"
    }
    val component: JComponent
        get() {
            return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(searchLabel)
                add(textFieldWithFileBrowser)
                add(searchButton)
            }
        }

    private val searchLabel = JLabel("Root Dir")
    private val textFieldWithFileBrowser = TextFieldWithBrowseButton{}.apply {
        text = PropertiesComponent.getInstance(project).getValue(PROPERTIES_ROOT_MODULE_DIR, "")
        addBrowseFolderListener(
            "Hello",
            "Choose the root directory containing project dependencies",
            project,
            FileChooserDescriptor(
                false,
                true,
                false,
                false,
                false,
                false
            )
        )
    }

    private val searchButton = JButton("Search").apply {
        addActionListener {
            searchListeners.forEach { it.invoke(textFieldWithFileBrowser.text) }
            PropertiesComponent.getInstance(project).setValue(PROPERTIES_ROOT_MODULE_DIR, textFieldWithFileBrowser.text)
        }
    }

    //region Listeners
    private val searchListeners = mutableListOf<(path: String)->Unit>()
    fun onSearch(callback: (path: String)->Unit) {
        searchListeners.add(callback)
    }
    //endregion Listeners
}