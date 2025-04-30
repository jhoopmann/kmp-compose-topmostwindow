package de.jhoopmann.stickywindow.compose.ui.window

import de.jhoopmann.stickywindow.compose.ui.awt.ComposeTopMostImpl

typealias CreateTopMostImplEvent<T> = ((ComposeTopMostImpl<T>) -> Unit)?