package view

import com.intellij.ui.BooleanTableCellRenderer
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import model.GradleBuild
import org.jdesktop.swingx.JXTable
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JComponent
import javax.swing.table.AbstractTableModel

class BuildTableView {
    val component: JComponent
        get() {
            return JBScrollPane(table)
        }

    private val tableModel = BuildTableModel()

    private val table = JBTable().apply {
        model = tableModel
        setDefaultEditor(Boolean::class.java, JXTable.BooleanEditor())
        setDefaultRenderer(Boolean::class.java, BooleanTableCellRenderer())
        addMouseListener(object: MouseListener {
            override fun mouseClicked(e: MouseEvent?) {
                e?.point.let {
                    val row = rowAtPoint(e?.point)
                    val col = columnAtPoint(e?.point)
                    if (col == 0) {
                        terminalOpenListeners.forEach { it.invoke(tableModel.builds[row]) }
                    }
                }
            }
            override fun mousePressed(e: MouseEvent?) {}
            override fun mouseReleased(e: MouseEvent?) {}
            override fun mouseEntered(e: MouseEvent?) {}
            override fun mouseExited(e: MouseEvent?) {}
        })
    }

    fun setBuilds(builds: List<GradleBuild>) {
        tableModel.builds = builds
        tableModel.fireTableDataChanged()
    }

    fun getBuilds(): List<GradleBuild> {
        return tableModel.builds
    }

    private inner class BuildTableModel: AbstractTableModel() {
        var builds = listOf<GradleBuild>()
        private val columnNames = arrayOf("project", "substitute", "using", "version", "status", "include")

        override fun getRowCount(): Int {
            return builds.size
        }

        override fun getColumnCount(): Int {
            return columnNames.size
        }

        override fun isCellEditable(row: Int, col: Int): Boolean {
            return col == 1 || col == 2 || col == 3 || col == 5
        }

        override fun getColumnClass(column: Int): Class<*> {
            return when(column) {
                0 -> String::class.java
                1 -> String::class.java
                2 -> String::class.java
                3 -> String::class.java
                4 -> String::class.java
                5 -> Boolean::class.java
                else -> Object::class.java
            }
        }

        override fun getColumnName(col: Int): String? {
            return columnNames[col]
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val build = builds[rowIndex]
            return when (columnIndex) {
                0 -> build.rootProjectName
                1 -> build.inclusionSettings.substitute ?: ""
                2 -> build.inclusionSettings.using ?: ""
                3 -> build.repoStatus?.checkedOutTag ?: ""
                4 -> build.repoStatus?.let { if (it.isClean) "clean" else "dirty" } ?: ""
                5 -> build.inclusionSettings.include
                else -> "NaN"
            }
        }

        override fun setValueAt(value: Any, row: Int, col: Int) {
            val build = builds[row]
            when (col) {
                0 -> { }
                1 -> {
                    build.inclusionSettings?.substitute = value as String
                    substitutionChangeListeners.forEach { it.invoke(build, value) }
                }
                2 -> {
                    build.inclusionSettings?.using = value as String
                    buildUsageListeners.forEach { it.invoke(build, value) }
                }
                3 -> { }
                4 -> { }
                5 -> {
                    build.inclusionSettings?.include = value as Boolean
                    inclusionChangeListeners.forEach { it.invoke(build, value) }
                }
            }
            fireTableCellUpdated(row, col)
        }
    }

    //region Listeners
    private val terminalOpenListeners = mutableListOf<(GradleBuild)->Unit>()
    fun onTerminalOpenRequest(callback: (dependency: GradleBuild)->Unit) {
        terminalOpenListeners.add(callback)
    }

    private val inclusionChangeListeners = mutableListOf<(build: GradleBuild, include: Boolean) -> Unit>()
    fun onBuildInclusionChange(callback: (build: GradleBuild, include: Boolean) -> Unit) {
        inclusionChangeListeners.add(callback)
    }

    private val substitutionChangeListeners = mutableListOf<(build: GradleBuild, substitute: String) -> Unit>()
    fun onBuildSubstitutionChange(callback: (build: GradleBuild, substitute: String) -> Unit) {
        substitutionChangeListeners.add(callback)
    }

    private val buildUsageListeners = mutableListOf<(build: GradleBuild, using: String) -> Unit>()
    fun onBuildUsageChange(callback: (build: GradleBuild, using: String) -> Unit) {
        buildUsageListeners.add(callback)
    }
    //endregion Listeners
}