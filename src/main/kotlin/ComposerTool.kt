import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.BooleanTableCellRenderer
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManager
import com.intellij.ui.table.JBTable
import org.jdesktop.swingx.JXTable
import org.jetbrains.plugins.terminal.ShellTerminalWidget
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory
import org.jetbrains.plugins.terminal.TerminalView
import org.jetbrains.plugins.terminal.arrangement.TerminalWorkingDirectoryManager
import java.awt.FlowLayout
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.io.File
import java.io.IOException
import java.util.*
import javax.swing.*
//1. remove based on project path, not current include expression
//2. add simple include expression if substitute & using aren't defined

//3. perform search in background
//4. persist substitute & using settings for projects
//5. clean up, test & refactor

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
        val dirPath = PropertiesComponent.getInstance(project).getValue("com.az.compositehelper.moduledir", "")
        add(JLabel("Root Dir"))
        val textFieldWithBrowseButton = TextFieldWithBrowseButton{}.apply {
            text = dirPath
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
                projects.forEach { proj -> proj.includeBuild = projectDependencyManager.isDependencyIncluded(proj) }
                tableModel.update(projects)
                PropertiesComponent.getInstance(project).setValue("com.az.compositehelper.moduledir", textFieldWithBrowseButton.text)
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

    fun openTerminal(workingDirectory: String, tabName: String) {
        //based on this: https://github.com/JetBrains/intellij-community/blob/95ab6a1ecfdf49754f7eb5a81984cdc2c4fa0ca5/plugins/sh/src/com/intellij/sh/run/ShTerminalRunner.java
        //looks like beyond plugin extensions, we can use classes defined in other plugins as well. In this case, we use TerminalView from intelliJs Terminal plugin.
        val terminalView = TerminalView.getInstance(project)
        val window = ToolWindowManager.getInstance(project).getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID) ?: return
        val contentManager = window.contentManager
        val pair = getSuitableProcess(contentManager, workingDirectory)
        try {
            if (pair == null) {
                terminalView.createLocalShellWidget(workingDirectory, tabName)
                return
            }
            window.activate(null)
            contentManager.setSelectedContent(pair.first!!)
        } catch (e: IOException) {

        }
    }

    private fun getSuitableProcess(contentManager: ContentManager, workingDirectory: String): Pair<Content?, ShellTerminalWidget?>? {
        val selectedContent = contentManager.selectedContent
        if (selectedContent != null) {
            val pair = getSuitableProcess(selectedContent, workingDirectory)
            if (pair != null) return pair
        }
        return Arrays.stream(contentManager.contents)
            .map { content -> getSuitableProcess(content, workingDirectory) }
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null)
    }

    private fun getSuitableProcess(content: Content, workingDirectory: String): Pair<Content?, ShellTerminalWidget?>? {
        val widget = TerminalView.getWidgetByContent(content) as? ShellTerminalWidget ?: return null
        val currentWorkingDirectory = TerminalWorkingDirectoryManager.getWorkingDirectory(widget, null)
        return if (currentWorkingDirectory == workingDirectory) {
            Pair(content, widget)
        } else {
            null
        }
    }
}