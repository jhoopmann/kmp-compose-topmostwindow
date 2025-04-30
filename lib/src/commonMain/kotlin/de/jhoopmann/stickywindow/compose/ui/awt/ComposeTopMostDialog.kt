package de.jhoopmann.stickywindow.compose.ui.awt

import androidx.compose.ui.awt.ComposeDialog
import de.jhoopmann.stickywindow.awt.ui.TopMostImpl

class ComposeTopMostDialog internal constructor(
    composeDialog: ComposeDialog = ComposeDialog(),
    delegate: TopMostImpl = TopMostImpl()
) : ComposeTopMostImpl<ComposeDialog>(composeDialog, delegate)
