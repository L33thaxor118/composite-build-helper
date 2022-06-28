import javax.swing.table.AbstractTableModel

class ProjectTableModel: AbstractTableModel() {
    var projects = listOf<ProjectDependency>()
    private val columnNames = arrayOf("project", "substitute", "using", "version", "status", "include")

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
        val project = projects[rowIndex]
        return when (columnIndex) {
            0 -> project.name
            1 -> project.substitute ?: ""
            2 -> project.using ?: ""
            3 -> project.checkedOutVersion
            4 -> if (project.isClean) "clean" else "dirty"
            5 -> project.includeBuild
            else -> "NaN"
        }
    }

    override fun setValueAt(value: Any, row: Int, col: Int) {
        val project = projects[row]
        when (col) {
            0 -> {

            }
            1 -> {
                project.substitute = value as String
            }
            2 -> {
                project.using = value as String
            }
            3 -> {
            }
            4 -> {

            }
            5 -> {
                project.includeBuild = value as Boolean
                listener?.invoke(project, value)
            }
        }
        fireTableCellUpdated(row, col)
    }

    var listener: IncludeListener? = null
    fun addIncludeListener(listener: IncludeListener) {
        this.listener = listener
    }
}

typealias IncludeListener = (project: ProjectDependency, include: Boolean)->Unit