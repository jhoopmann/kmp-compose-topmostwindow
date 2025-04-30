package de.jhoopmann.stickywindow.compose.ui.awt

import androidx.compose.ui.awt.ComposeWindow
import de.jhoopmann.stickywindow.awt.ui.TopMostImpl

class ComposeTopMostWindow internal constructor(
    composeWindow: ComposeWindow = ComposeWindow(),
    delegate: TopMostImpl = TopMostImpl()
) : ComposeTopMostImpl<ComposeWindow>(composeWindow, delegate)