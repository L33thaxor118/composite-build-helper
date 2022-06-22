import javax.swing.table.AbstractTableModel

class ProjectTableModel: AbstractTableModel() {
    var projects = listOf<ProjectDependency>()
    private val columnNames = arrayOf("Project", "Include")

    fun update(newProjects: List<ProjectDependency>) {
        projects = newProjects
    }

    override fun getRowCount(): Int {
        return projects.size
    }

    override fun getColumnCount(): Int {
        return columnNames.size
    }

    override fun isCellEditable(row: Int, col: Int): Boolean {
        return col == 1
    }

    override fun getColumnClass(column: Int): Class<*>? {
        return if (column == 1) {
            Boolean::class.java
        } else String::class.java
    }

    override fun getColumnName(col: Int): String? {
        return columnNames[col]
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val project = projects[rowIndex]
        return when (columnIndex) {
            0 -> project.name
            1 -> project.includeBuild
            else -> "NaN"
        }
    }

    override fun setValueAt(value: Any, row: Int, col: Int) {
        val project = projects[row]
        when (col) {
            1 -> {
                project.includeBuild = value as Boolean
                listener?.invoke(project.name, project.path, value as Boolean)
            }
        }
        fireTableCellUpdated(row, col)
    }

    var listener: IncludeListener? = null
    fun addIncludeListener(listener: IncludeListener) {
        this.listener = listener
    }
}

typealias IncludeListener = (projName: String, projPath: String, include: Boolean)->Unit