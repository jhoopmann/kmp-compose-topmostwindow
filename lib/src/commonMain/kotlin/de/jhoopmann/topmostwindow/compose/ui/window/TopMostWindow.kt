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

package de.jhoopmann.topmostwindow.compose.ui.window

import androidx.compose.runtime.*
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.util.packFloats
import androidx.compose.ui.window.*
import de.jhoopmann.topmostwindow.awt.ui.TopMost
import de.jhoopmann.topmostwindow.awt.ui.TopMostCompanion
import de.jhoopmann.topmostwindow.awt.ui.TopMostOptions
import de.jhoopmann.topmostwindow.compose.ui.awt.ComposeTopMostWindow
import de.jhoopmann.topmostwindow.compose.ui.util.ComposeWindowHelper
import java.awt.Component
import java.awt.Window
import java.awt.event.*
import javax.swing.JFrame
import kotlin.reflect.full.companionObjectInstance

/**
 * topMost (Natively sets Window above all other Windows)
 * sticky (Natively sets Window to appear on all Spaces)
 * skipTaskbar (Natively hides Window from taskbar):
 *  has no effect on macOS because non mainWindows never appear in Dock anyway, use sticky.
 *  has no effect on windows because non toolbox window without parent always appear in taskbar, use sticky.
 */
@Composable
fun TopMostWindow(
    onCloseRequest: () -> Unit,
    state: WindowState = rememberWindowState(),
    visible: Boolean = true,
    topMost: Boolean = true,
    sticky: Boolean = true,
    skipTaskbar: Boolean = true,
    title: String = "Untitled",
    icon: Painter? = null,
    undecorated: Boolean = false,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    beforeInitialization: ((TopMost, TopMostOptions) -> Unit)? = { topMost, options ->
        (topMost::class.companionObjectInstance as TopMostCompanion).setPlatformOptionsBeforeInit(options)
    },
    afterInitialization: ((TopMost, TopMostOptions) -> Unit)? = { topMost, options ->
        (topMost::class.companionObjectInstance as TopMostCompanion).setPlatformOptionsAfterInit(options)
    },
    create: () -> ComposeTopMostWindow = { ComposeTopMostWindow() },
    content: @Composable FrameWindowScope.() -> Unit,
) {
    val currentState: WindowState by rememberUpdatedState(state)
    val currentTitle: String by rememberUpdatedState(title)
    val currentIcon: Painter? by rememberUpdatedState(icon)
    val currentUndecorated: Boolean by rememberUpdatedState(undecorated)
    val currentTransparent: Boolean by rememberUpdatedState(transparent)
    val currentResizable: Boolean by rememberUpdatedState(resizable)
    val currentEnabled: Boolean by rememberUpdatedState(enabled)
    val currentFocusable: Boolean by rememberUpdatedState(focusable)
    val currentOnCloseRequest: () -> Unit by rememberUpdatedState(onCloseRequest)
    val currentTopMost: Boolean by rememberUpdatedState(topMost)
    val currentSticky: Boolean by rememberUpdatedState(sticky)
    val currentSkipTaskbar: Boolean by rememberUpdatedState(skipTaskbar)

    val updater = remember { ComposeWindowHelper.getComponentUpdater() }

    val appliedState = remember {
        object {
            var size: DpSize? = null
            var position: WindowPosition? = null
            var placement: WindowPlacement? = null
            var isMinimized: Boolean? = null
        }
    }

    val listeners = remember {
        object {
            var windowListenerRef: Any =
                ComposeWindowHelper.getListenerOnWindowRef(Window::addWindowListener, Window::removeWindowListener)
            var windowStateListenerRef: Any =
                ComposeWindowHelper.getListenerOnWindowRef(
                    Window::addWindowStateListener,
                    Window::removeWindowStateListener
                )
            var componentListenerRef: Any =
                ComposeWindowHelper.getListenerOnWindowRef(
                    Component::addComponentListener,
                    Component::removeComponentListener
                )

            fun removeFromAndClear(window: ComposeWindow) {
                ComposeWindowHelper.getListenerUnregisterMethod().apply {
                    call(windowListenerRef, window)
                    call(windowStateListenerRef, window)
                    call(componentListenerRef, window)
                }
            }
        }
    }

    var composeTopMostWindow: ComposeTopMostWindow? by remember { mutableStateOf(null) }
    var initialized: Boolean by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        composeTopMostWindow?.setVisible(visible)
    }

    Window(
        visible = false, // dont pass visibility, we will handle it via ComposeTopMostWindow
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
        create = {
            create().let { it ->
                composeTopMostWindow = it
                initialized = false

                it.composeWindow.apply {
                    defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE

                    ComposeWindowHelper.getListenerRegisterMethod<WindowListener>().call(
                        listeners.windowListenerRef,
                        this,
                        object : WindowAdapter() {
                            override fun windowClosing(e: WindowEvent) {
                                currentOnCloseRequest.invoke()
                            }
                        }
                    )

                    ComposeWindowHelper.getListenerRegisterMethod<WindowStateListener>().call(
                        listeners.windowStateListenerRef,
                        this,
                        object : WindowStateListener {
                            override fun windowStateChanged(p0: WindowEvent?) {
                                currentState.isMinimized = isMinimized
                                appliedState.placement = currentState.placement
                                appliedState.isMinimized = currentState.isMinimized
                            }
                        }
                    )

                    ComposeWindowHelper.getListenerRegisterMethod<ComponentAdapter>().call(
                        listeners.componentListenerRef,
                        this,
                        object : ComponentAdapter() {
                            override fun componentResized(e: ComponentEvent) {
                                currentState.placement = placement
                                currentState.size = DpSize(width.dp, height.dp)
                                appliedState.placement = currentState.placement
                                appliedState.size = currentState.size
                            }

                            override fun componentMoved(e: ComponentEvent) {
                                currentState.position = WindowPosition(x.dp, y.dp)
                                appliedState.position = currentState.position
                            }
                        }
                    )

                    ComposeWindowHelper.getWindowLocationTracker().let { tracker ->
                        ComposeWindowHelper.getWindowLocationTrackerOnWindowCreatedMethod().call(
                            tracker,
                            this
                        )
                    }
                }
            }
        },
        update = { window ->
            ComposeWindowHelper.getComponentUpdaterUpdateMethod().call(
                updater,
                object : Function1<Any, Unit> {
                    override fun invoke(updateScope: Any) {
                        fun update() {
                            ComposeWindowHelper.getUpdateScopeSetMethod().apply {
                                call(
                                    updateScope, currentUndecorated,
                                    { it: Boolean ->
                                        ComposeWindowHelper.getWindowSetUndecoratedSafelyMethod()
                                            .call(window, it)
                                    })
                                call(updateScope, currentTitle, window::setTitle)
                                call(
                                    updateScope, currentIcon,
                                    { it: Painter? ->
                                        ComposeWindowHelper.getWindowSetIconMethod().call(window, it)
                                    })
                                call(updateScope, currentTransparent, window::isTransparent::set)
                                call(updateScope, currentEnabled, window::setEnabled)
                                call(updateScope, currentFocusable, window::setFocusableWindowState)
                            }

                            if (state.size != appliedState.size) {
                                val width = state.size.takeUnless { it.isUnspecified }
                                    ?.width?.takeUnless { it.isUnspecified }?.value ?: Float.NaN
                                val height = state.size.takeUnless { it.isUnspecified }
                                    ?.height?.takeUnless { it.isUnspecified }?.value ?: Float.NaN

                                ComposeWindowHelper.getWindowSetSizeSafelyMethod().invoke(
                                    null,
                                    window,
                                    packFloats(width, height),
                                    state.placement
                                )
                                appliedState.size = state.size
                            }

                            if (state.position != appliedState.position) {
                                ComposeWindowHelper.getWindowSetPositionSafelyMethod().invoke(
                                    null,
                                    window,
                                    state.position,
                                    state.placement,
                                    {
                                        ComposeWindowHelper.getWindowLocationTracker().let { tracker ->
                                            ComposeWindowHelper.getWindowLocationTrackerGetCascadeLocationForMethod()
                                                .call(tracker, window)
                                        }
                                    }
                                )

                                appliedState.position = state.position
                            }
                            if (state.placement != appliedState.placement) {
                                window.placement = state.placement
                                appliedState.placement = state.placement
                            }
                            if (state.isMinimized != appliedState.isMinimized) {
                                window.isMinimized = state.isMinimized
                                appliedState.isMinimized = state.isMinimized
                            }

                            /* on macOS we have to apply resizsable after creation of NSWindow */
                            ComposeWindowHelper.getUpdateScopeSetMethod().apply {
                                call(updateScope, currentResizable, window::setResizable)
                            }
                        }

                        if (!initialized) {
                            initialized = true

                            with(composeTopMostWindow!!) {
                                TopMostOptions(
                                    topMost = currentTopMost,
                                    sticky = currentSticky,
                                    skipTaskbar = currentSkipTaskbar
                                ).also { options ->
                                    initialize(window, options, {
                                        update()

                                        window.windowHandle
                                    }, beforeInitialization, afterInitialization)
                                }
                            }
                        } else {
                            update()
                        }
                    }
                }
            )
        },
        dispose = {
            ComposeWindowHelper.getWindowLocationTracker().let { tracker ->
                ComposeWindowHelper.getWindowLocationTrackerOnWindowDisposedMethod()
                    .call(tracker, it)
            }
            listeners.removeFromAndClear(it)
            it.dispose()
        },
        content = content
    )
}
