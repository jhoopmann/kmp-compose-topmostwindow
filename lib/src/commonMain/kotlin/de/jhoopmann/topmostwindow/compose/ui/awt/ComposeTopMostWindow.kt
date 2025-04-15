package de.jhoopmann.topmostwindow.compose.ui.awt

import androidx.compose.ui.awt.ComposeWindow
import de.jhoopmann.topmostwindow.awt.ui.TopMost
import de.jhoopmann.topmostwindow.awt.ui.TopMostImpl

open class ComposeTopMostWindow(
    open val composeWindow: ComposeWindow = ComposeWindow(),
    open val delegate: TopMostImpl = TopMostImpl()
) : TopMost by delegate {
    open lateinit var update: (ComposeWindow) -> Unit

    open fun setVisible(visible: Boolean) {
        if (visible) {
            update.invoke(composeWindow)
        }

        setVisible(visible) {
            composeWindow.setVisible(visible)
        }
    }
}
