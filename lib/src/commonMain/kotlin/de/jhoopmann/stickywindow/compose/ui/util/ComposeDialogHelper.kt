package de.jhoopmann.stickywindow.compose.ui.util

import androidx.compose.runtime.CompositionLocal
import java.awt.Dialog
import java.awt.Window
import kotlin.reflect.KFunction2
import kotlin.reflect.jvm.kotlinFunction

object ComposeDialogHelper {
    internal fun getDialogSetUndecoratedSafelyMethod(): KFunction2<Dialog, Boolean, Unit> {
        return Class.forName("androidx.compose.ui.util.Windows_desktopKt")
            .getDeclaredMethod("setUndecoratedSafely", Dialog::class.java, Boolean::class.java).apply {
                isAccessible = true
            }.kotlinFunction as KFunction2<Dialog, Boolean, Unit>
    }

    fun getLocalWindow(): CompositionLocal<Window?> {
        return Class.forName("androidx.compose.ui.window.LocalWindowKt")
            .getDeclaredField("LocalWindow").apply {
                isAccessible = true
            }.get(null) as CompositionLocal<Window?>
    }
}