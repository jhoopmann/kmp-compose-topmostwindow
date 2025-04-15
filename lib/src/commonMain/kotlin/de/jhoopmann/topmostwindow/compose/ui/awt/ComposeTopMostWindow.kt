package de.jhoopmann.topmostwindow.compose.ui.awt

import androidx.compose.ui.awt.ComposeWindow
import de.jhoopmann.topmostwindow.awt.ui.TopMost
import de.jhoopmann.topmostwindow.awt.ui.TopMostImpl

class ComposeTopMostWindow internal constructor(
    val composeWindow: ComposeWindow = ComposeWindow(),
    private val delegate: TopMostImpl = TopMostImpl()
) : TopMost by delegate {
    internal lateinit var update: (ComposeWindow) -> Unit

    fun setVisible(visible: Boolean) {
        if (visible) {
            update.invoke(composeWindow)
        }

        setVisible(visible) {
            composeWindow.setVisible(visible)
        }
    }
}
