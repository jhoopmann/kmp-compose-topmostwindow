package de.jhoopmann.topmostwindow.compose.ui.window

import de.jhoopmann.topmostwindow.compose.ui.awt.ComposeTopMostImpl

typealias CreateTopMostImplEvent<T> = ((ComposeTopMostImpl<T>) -> Unit)?