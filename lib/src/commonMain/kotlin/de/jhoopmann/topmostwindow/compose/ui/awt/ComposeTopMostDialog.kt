package de.jhoopmann.topmostwindow.compose.ui.awt

import androidx.compose.ui.awt.ComposeDialog
import de.jhoopmann.topmostwindow.awt.ui.TopMostImpl

class ComposeTopMostDialog internal constructor(
    composeDialog: ComposeDialog = ComposeDialog(),
    delegate: TopMostImpl = TopMostImpl()
) : ComposeTopMostImpl<ComposeDialog>(composeDialog, delegate)
