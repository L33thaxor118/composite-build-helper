import javax.swing.table.AbstractTableModel

class ProjectTableModel: AbstractTableModel() {
    var projects = listOf<ProjectItem>()

    fun update(newProjects: List<ProjectItem>) {
        projects = newProjects
    }

    override fun getRowCount(): Int {
        return projects.size
    }

    override fun getColumnCount(): Int {
        return 2
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val project = projects[rowIndex]
        return when (columnIndex) {
            0 -> project.name
            1 -> project.path
            else -> "NaN"
        }
    }
}