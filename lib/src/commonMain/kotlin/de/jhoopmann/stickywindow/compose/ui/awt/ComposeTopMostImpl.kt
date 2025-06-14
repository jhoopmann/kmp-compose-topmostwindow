package de.jhoopmann.stickywindow.compose.ui.awt

import de.jhoopmann.stickywindow.awt.ui.TopMost
import de.jhoopmann.stickywindow.awt.ui.TopMostImpl
import java.awt.Window

open class ComposeTopMostImpl<T : Window> internal constructor(
    val window: T,
    private val delegate: TopMostImpl = TopMostImpl()
) : TopMost by delegate {
    internal lateinit var update: (T) -> Unit

    var isVisible: Boolean
        get() = window.isVisible
        set(value) {
            update.takeIf { value }?.invoke(window)

            delegate.setVisible(value) {
                window.isVisible = it
            }
        }
}
