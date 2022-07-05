package view

import com.intellij.ui.components.JBScrollPane
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class ControlsView {
    val component: JComponent
        get() {
            return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(refreshButton)
                add(updateButton)
            }
        }

    private val refreshButton = JButton("Refresh Git Status").apply {
        addActionListener {
            refreshListeners.forEach { it.invoke() }
        }
    }
    private val updateButton = JButton("Update settings.gradle").apply {
        addActionListener {
            updateListeners.forEach { it.invoke() }
        }
    }

    //region Listeners
    private val refreshListeners = mutableListOf<()->Unit>()
    fun onGitRefresh(callback: ()->Unit) {
        refreshListeners.add(callback)
    }

    private val updateListeners = mutableListOf<()->Unit>()
    fun onUpdateSettings(callback: ()->Unit) {
        updateListeners.add(callback)
    }
    //endregion Listeners
}