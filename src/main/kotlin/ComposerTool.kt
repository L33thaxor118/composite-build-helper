import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.BooleanTableCellRenderer
import com.intellij.ui.table.JBTable
import org.jdesktop.swingx.JXTable
import java.awt.FlowLayout
import java.io.File
import javax.swing.*

class ComposerTool(project: Project) {
    private val container = JPanel()
    fun getView(): JPanel = container

    private val tableModel = ProjectTableModel()

    private val projects = JBTable().apply {
        model = tableModel
        setDefaultEditor(Boolean::class.java, JXTable.BooleanEditor())
        setDefaultRenderer(Boolean::class.java, BooleanTableCellRenderer())
        tableModel.addIncludeListener { projName, projPath, include ->

        }
    }

    private val rootDirPicker = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        add(JLabel("Root Dir"))
        val textFieldWithBrowseButton = TextFieldWithBrowseButton{}.apply {
            text = "/Users/azrielalvarado/AndroidStudioProjects"
            addBrowseFolderListener(
                "What...",
                "What?",
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
        add(textFieldWithBrowseButton)
        add(JButton("Search").apply {
            addActionListener {
                val dirs = findProjects(textFieldWithBrowseButton.text)
                val projects = dirs.mapNotNull { ProjectItem.from(it) }
                tableModel.update(projects)
            }
        })
    }

    init {
        initViews()
    }

    private fun initViews() {
        container.layout = BoxLayout(container, BoxLayout.Y_AXIS)
        container.add(rootDirPicker)
        container.add(JScrollPane(projects))
    }

    private fun findProjects(path: String): List<File> =
        File(path).walk().filter { it.isDirectory }.filter { File(it.path, "settings.gradle").exists() }.toList()
}