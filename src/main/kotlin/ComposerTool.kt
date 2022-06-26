import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.BooleanTableCellRenderer
import com.intellij.ui.table.JBTable
import org.jdesktop.swingx.JXTable
import org.jetbrains.plugins.terminal.TerminalView
import java.awt.FlowLayout
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.io.File
import java.io.IOException
import javax.swing.*


class ComposerTool(private val project: Project) {
    private val container = JPanel()
    fun getView(): JPanel = container
    private val projectDependencyManager = ProjectDependencyManager(project)

    private val tableModel = ProjectTableModel()

    private val projects = JBTable().apply {
        model = tableModel
        setDefaultEditor(Boolean::class.java, JXTable.BooleanEditor())
        setDefaultRenderer(Boolean::class.java, BooleanTableCellRenderer())
        tableModel.addIncludeListener { dependency, include ->
            projectDependencyManager.updateProjectBuildInclusion(dependency, include)
        }
        addMouseListener(object: MouseListener {
            override fun mouseClicked(e: MouseEvent?) {
                e?.point.let {
                    val row = rowAtPoint(e?.point)
                    val col = columnAtPoint(e?.point)
                    if (col == 0) {
                        val project = tableModel.projects[row]
                        openTerminal(project.path, project.name)
                    }
                }
            }
            override fun mousePressed(e: MouseEvent?) {}
            override fun mouseReleased(e: MouseEvent?) {}
            override fun mouseEntered(e: MouseEvent?) {}
            override fun mouseExited(e: MouseEvent?) {}
        })
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
                val projects = dirs.mapNotNull { ProjectDependency(it) }
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

    fun openTerminal(path: String, tabName: String) {
        //based on this: https://github.com/JetBrains/intellij-community/blob/95ab6a1ecfdf49754f7eb5a81984cdc2c4fa0ca5/plugins/sh/src/com/intellij/sh/run/ShTerminalRunner.java
        //looks like beyond plugin extensions, we can use classes defined in other plugins as well. In this case, we use TerminalView from intelliJs Terminal plugin.
        val terminalView = TerminalView.getInstance(project)
        try {
            terminalView.createLocalShellWidget(path, tabName)
        } catch (e: IOException) {

        }
    }
}