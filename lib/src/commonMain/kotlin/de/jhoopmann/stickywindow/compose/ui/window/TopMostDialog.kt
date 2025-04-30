/*
 * Code partially copied from KMP Desktop Window.desktop.kt
 *
 * added parameters: topMost, sticky, skipTaskbar, create, beforeInitialization, afterInitialization
 *
 *
 * ORIGINAL LICENSE:
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.jhoopmann.stickywindow.compose.ui.window

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.*
import de.jhoopmann.stickywindow.awt.ui.DefaultPlatformBeforeInitialization
import de.jhoopmann.stickywindow.awt.ui.InitializationEvent
import de.jhoopmann.stickywindow.compose.ui.awt.ComposeTopMostDialog
import de.jhoopmann.stickywindow.compose.ui.util.ComposeDialogHelper
import java.awt.Dialog.ModalityType
import java.awt.Window

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TopMostDialog(
    onCloseRequest: () -> Unit,
    state: DialogState = rememberDialogState(),
    visible: Boolean = true,
    topMost: Boolean = true,
    sticky: Boolean = true,
    skipTaskbar: Boolean = true,
    title: String = "Untitled",
    icon: Painter? = null,
    decoration: WindowDecoration = WindowDecoration.SystemDefault,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    owner: Window? = ComposeDialogHelper.getLocalWindow().current,
    modalityType: ModalityType? = null,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    onCreate: CreateTopMostImplEvent<ComposeDialog> = null,
    beforeInitialization: InitializationEvent = DefaultPlatformBeforeInitialization,
    afterInitialization: InitializationEvent = DefaultPlatformBeforeInitialization,
    content: @Composable DialogWindowScope.() -> Unit,
) {
    TopMostImpl(
        onCloseRequest,
        state,
        visible,
        topMost,
        sticky,
        skipTaskbar,
        title,
        icon,
        decoration,
        transparent,
        resizable,
        enabled,
        focusable,
        onPreviewKeyEvent,
        onKeyEvent,
        onCreate,
        beforeInitialization,
        afterInitialization,
        { onPreviewKeyEvent, onKeyEvent, applyCreateOptions, dispose, updateWithInitialize ->
            DialogWindow(
                false, // dont pass visibility, we handle it via ComposeTopMostWindow
                onPreviewKeyEvent,
                onKeyEvent,
                {
                    applyCreateOptions.invoke(
                        ComposeTopMostDialog(
                            composeDialog = ComposeDialog(
                                owner,
                                modalityType
                                    ?: (ModalityType.DOCUMENT_MODAL.takeIf { owner != null } ?: ModalityType.MODELESS)
                            )
                        )
                    )
                },
                dispose,
                updateWithInitialize,
                content
            )
        }
    )
}
