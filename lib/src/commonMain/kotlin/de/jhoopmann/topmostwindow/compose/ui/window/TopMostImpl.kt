package de.jhoopmann.topmostwindow.compose.ui.window

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.util.packFloats
import androidx.compose.ui.window.*
import de.jhoopmann.topmostwindow.awt.ui.*
import de.jhoopmann.topmostwindow.compose.ui.awt.ComposeTopMostImpl
import de.jhoopmann.topmostwindow.compose.ui.awt.ComposeTopMostWindow
import de.jhoopmann.topmostwindow.compose.ui.util.ComposeDialogHelper
import de.jhoopmann.topmostwindow.compose.ui.util.ComposeWindowHelper
import java.awt.Component
import java.awt.Window
import java.awt.event.*
import javax.swing.JFrame

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T : Window, S : Any> TopMostImpl(
    onCloseRequest: () -> Unit,
    state: S,
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
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    onCreate: CreateTopMostImplEvent<T> = null,
    beforeInitialization: InitializationEvent = DefaultPlatformBeforeInitialization,
    afterInitialization: InitializationEvent = DefaultPlatformAfterInitialization,
    createWindow: @Composable (
        onPreviewKeyEvent: (KeyEvent) -> Boolean,
        onkeyEvent: (KeyEvent) -> Boolean,
        applyCreateOptions: (ComposeTopMostImpl<T>) -> T,
        dispose: (T) -> Unit,
        updateWithInitialize: (T) -> Unit
    ) -> Unit
) {
    val currentState: S by rememberUpdatedState(state)
    val currentTitle: String by rememberUpdatedState(title)
    val currentIcon: Painter? by rememberUpdatedState(icon)
    val currentDecoration: WindowDecoration by rememberUpdatedState(decoration)
    val currentTransparent: Boolean by rememberUpdatedState(transparent)
    val currentResizable: Boolean by rememberUpdatedState(resizable)
    val currentEnabled: Boolean by rememberUpdatedState(enabled)
    val currentFocusable: Boolean by rememberUpdatedState(focusable)
    val currentOnCloseRequest: () -> Unit by rememberUpdatedState(onCloseRequest)

    val topMostOptions: TopMostOptions = remember {
        TopMostOptions(
            topMost = topMost,
            sticky = sticky,
            skipTaskbar = skipTaskbar
        )
    }

    var currentInitialized: Boolean by remember { mutableStateOf(false) }
    var currentComposeTopMostWindowWindow: ComposeTopMostImpl<T>? by remember { mutableStateOf(null) }

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

            fun removeFromAndClear(window: T) {
                ComposeWindowHelper.getListenerUnregisterMethod().apply {
                    call(windowListenerRef, window)
//                    call(windowStateListenerRef, window)
                    call(componentListenerRef, window)
                }
            }
        }
    }

    val updater: Any = remember { ComposeWindowHelper.getComponentUpdater() } // internal class ComponentUpdater
    val updateWindow: (Any, T) -> Unit = remember {
        { updateScope, window ->
            ComposeWindowHelper.getUpdateScopeSetMethod().apply {
                val isUndecorated: Boolean =
                    ComposeWindowHelper.getWindowIsUndecorated(currentDecoration)
                val resizerThickness: Dp =
                    WindowDecorationDefaults.ResizerThickness.takeUnless { isUndecorated }
                        ?: ComposeWindowHelper.getResizerThicknessForWindowDecoration(currentDecoration)

                call(updateScope, currentTitle, (window as? ComposeDialog)?.let { it::setTitle }
                        ?: (window as ComposeWindow)::setTitle)
                call(
                    updateScope, currentIcon,
                    { it: Painter? ->
                        ComposeWindowHelper.getWindowSetIconMethod().call(window, it)
                    })
                call(
                    updateScope, isUndecorated,
                    { it: Boolean ->
                        (if (window is ComposeDialog) ComposeDialogHelper.getDialogSetUndecoratedSafelyMethod()
                        else ComposeWindowHelper.getWindowSetUndecoratedSafelyMethod())
                            .call(window, it)
                    })


                call(
                    updateScope, currentTransparent,
                    (window as? ComposeDialog)?.let { it::isTransparent::set }
                        ?: (window as ComposeWindow)::isTransparent::set)
                call(
                    updateScope,
                    resizerThickness,
                    (window as? ComposeDialog)?.let { it::undecoratedResizerThickness::set }
                        ?: (window as ComposeWindow)::undecoratedResizerThickness::set)

                call(updateScope, currentEnabled, window::setEnabled)
                call(updateScope, currentFocusable, window::setFocusableWindowState)
            }

            val size: DpSize = (state as? DialogState)?.size ?: (state as WindowState).size
            val placement: WindowPlacement =
                (state as? DialogState)?.run { WindowPlacement.Floating } ?: (state as WindowState).placement
            val position: WindowPosition = (state as? DialogState)?.position ?: (state as WindowState).position

            if (size != appliedState.size) {
                val width: Float = size.takeUnless { it.isUnspecified }
                    ?.width?.takeUnless { it.isUnspecified }?.value ?: Float.NaN
                val height: Float = size.takeUnless { it.isUnspecified }
                    ?.height?.takeUnless { it.isUnspecified }?.value ?: Float.NaN

                ComposeWindowHelper.getWindowSetSizeSafelyMethod().invoke(
                    null,
                    window,
                    packFloats(width, height),
                    placement
                )
                appliedState.size = size
            }

            if (position != appliedState.position) {
                ComposeWindowHelper.getWindowSetPositionSafelyMethod().invoke(
                    null,
                    window,
                    position,
                    placement,
                    {
                        ComposeWindowHelper.getWindowLocationTracker().let { tracker ->
                            ComposeWindowHelper.getWindowLocationTrackerGetCascadeLocationForMethod()
                                .call(tracker, window)
                        }
                    }
                )
                appliedState.position = position
            }

            if (window is ComposeWindow && state is WindowState) {
                if (state.placement != appliedState.placement) {
                    window.placement = state.placement
                    appliedState.placement = state.placement
                }
                if (state.isMinimized != appliedState.isMinimized) {
                    window.isMinimized = state.isMinimized
                    appliedState.isMinimized = state.isMinimized
                }
            }

            /* on macOS we have to apply resizsable after creation of NSWindow */
            ComposeWindowHelper.getUpdateScopeSetMethod().apply {
                call(
                    updateScope,
                    currentResizable,
                    (window as? ComposeDialog)?.let { it::setResizable } ?: (window as ComposeWindow)::setResizable)
            }
        }
    }


    val updateWithInitialize: (T) -> Unit = remember {
        { window ->
            ComposeWindowHelper.getComponentUpdaterUpdateMethod().call(
                updater,
                object : Function1<Any, Unit> {
                    override fun invoke(updateScope: Any) {
                        if (!currentInitialized) {
                            currentInitialized = true

                            currentComposeTopMostWindowWindow!!.initialize(window, topMostOptions, {
                                updateWindow.invoke(updateScope, window)

                                DefaultPlatformInitializeParent.invoke(this, window)
                            }, beforeInitialization, afterInitialization)
                        } else {
                            updateWindow(updateScope, window)
                        }
                    }
                }
            )
        }
    }

    val applyCreateOptions: (ComposeTopMostImpl<T>) -> T = remember {
        {
            currentComposeTopMostWindowWindow = it.apply {
                this.update = updateWithInitialize
                onCreate?.invoke(this)
            }

            it.window.apply {
                val closeOperation: Int = JFrame.DO_NOTHING_ON_CLOSE
                (this as? ComposeDialog)?.apply {
                    defaultCloseOperation = closeOperation
                } ?: (this as ComposeWindow).apply {
                    defaultCloseOperation = closeOperation
                }

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
                            (currentState as? WindowState)?.let {
                                with(this@apply as ComposeWindow) {
                                    it.placement = placement
                                    it.isMinimized = isMinimized
                                    appliedState.placement = it.placement
                                    appliedState.isMinimized = it.isMinimized
                                }
                            }
                        }
                    }
                )

                ComposeWindowHelper.getListenerRegisterMethod<ComponentAdapter>().call(
                    listeners.componentListenerRef,
                    this,
                    object : ComponentAdapter() {
                        override fun componentResized(e: ComponentEvent) {
                            (currentState as? WindowState)?.let {
                                with(this@apply as ComposeWindow) {
                                    it.placement = placement
                                    it.size = DpSize(width.dp, height.dp)
                                    appliedState.placement = it.placement
                                    appliedState.size = it.size
                                }
                            } ?: (currentState as DialogState).let {
                                with(this@apply as ComposeDialog) {
                                    it.size = DpSize(width.dp, height.dp)
                                    appliedState.size = it.size
                                }
                            }
                        }

                        override fun componentMoved(e: ComponentEvent) {
                            (currentState as? WindowState)?.let {
                                with(this@apply as ComposeWindow) {
                                    it.position = WindowPosition(x.dp, y.dp)
                                    appliedState.position = it.position
                                }
                            } ?: (currentState as DialogState).let {
                                with(this@apply as ComposeDialog) {
                                    it.position = WindowPosition(x.dp, y.dp)
                                    appliedState.position = it.position
                                }
                            }

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
    }

    val dispose: (T) -> Unit = remember {
        {
            currentInitialized = false

            ComposeWindowHelper.getWindowLocationTracker().let { tracker ->
                ComposeWindowHelper.getWindowLocationTrackerOnWindowDisposedMethod()
                    .call(tracker, it)
            }
            listeners.removeFromAndClear(it)
            it.dispose()

            currentComposeTopMostWindowWindow = null
        }
    }


    createWindow.invoke(
        onPreviewKeyEvent,
        onKeyEvent,
        applyCreateOptions,
        dispose,
        updateWithInitialize
    )

    LaunchedEffect(visible) {
        currentComposeTopMostWindowWindow?.isVisible = visible
    }
}