package de.jhoopmann.topmostwindow.compose.ui.util

import androidx.compose.ui.graphics.painter.Painter
import java.awt.Frame
import java.awt.Window
import java.lang.reflect.Method
import kotlin.reflect.*
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction

object ComposeWindowHelper {
    fun getAccessibleClassConstructor(name: String, vararg parameters: KType): KFunction<Any> {
        return Class.forName(name).kotlin.constructors.find { c ->
            c.parameters.filterIndexed { i, p ->
                p.type.classifier is KTypeParameter || p.type.classifier == parameters[i].classifier
            }.size == c.parameters.size
        }?.apply {
            isAccessible = true
        } ?: throw RuntimeException("Constructor for $name and $parameters not found")
    }

    fun getClassSubClass(cls: String, name: String): KClass<*> {
        return Class.forName(cls).kotlin.nestedClasses.find {
            it.simpleName == name
        } ?: throw RuntimeException("Subclass $name in $cls not found")
    }

    fun getAccessibleClassMethod(cls: String, name: String, vararg parameters: KType): KFunction<*> {
        return Class.forName(cls).kotlin.declaredFunctions.find {
            it.name == name && it.valueParameters.filterIndexed { i, p ->
                p.type.classifier is KTypeParameter || p.type.classifier == parameters[i].classifier
            }.size == it.valueParameters.size
        }?.apply {
            isAccessible = true
        } ?: throw RuntimeException("Method $name on $cls not found")
    }

    fun <T> getListenerOnWindowRef(
        register: Window.(T) -> Unit,
        unregister: Window.(T) -> Unit,
    ): Any {
        return getAccessibleClassConstructor(
            "androidx.compose.ui.util.ListenerOnWindowRef",
            typeOf<Function2<Window, T, Unit>>(),
            typeOf<Function2<Window, T, Unit>>()

        ).call(register, unregister)
    }

    inline fun <reified T> getListenerRegisterMethod(): KFunction3<Any, Window, T, Unit> {
        return getAccessibleClassMethod(
            "androidx.compose.ui.util.ListenerOnWindowRef",
            "registerWithAndSet",
            typeOf<Window>(),
            typeOf<T>()
        ) as KFunction3<Any, Window, T, Unit>
    }

    fun getListenerUnregisterMethod(): KFunction<*> {
        return getAccessibleClassMethod(
            "androidx.compose.ui.util.ListenerOnWindowRef",
            "unregisterFromAndClear",
            typeOf<Window>()
        )
    }

    fun getWindowLocationTracker(): Any {
        return Class.forName("androidx.compose.ui.window.WindowLocationTracker")
            .getField("INSTANCE").get(null)
    }

    fun getWindowLocationTrackerMethod(name: String, vararg parameters: KType): KFunction<*> {
        return getAccessibleClassMethod(
            "androidx.compose.ui.window.WindowLocationTracker",
            name,
            *parameters
        )
    }

    fun getWindowLocationTrackerOnWindowCreatedMethod(): KFunction<*> {
        return getWindowLocationTrackerMethod("onWindowCreated", typeOf<Window>())
    }

    fun getWindowLocationTrackerOnWindowDisposedMethod(): KFunction<*> {
        return getWindowLocationTrackerMethod("onWindowDisposed", typeOf<Window>())
    }

    fun getWindowLocationTrackerGetCascadeLocationForMethod(): KFunction<*> {
        return getWindowLocationTrackerMethod("getCascadeLocationFor", typeOf<Window>())
    }

    fun getComponentUpdater(): Any {
        return getAccessibleClassConstructor("androidx.compose.ui.util.ComponentUpdater").call()
    }

    fun getComponentUpdaterUpdateMethod(): KFunction<*> {
        return getAccessibleClassMethod(
            "androidx.compose.ui.util.ComponentUpdater",
            "update",
            typeOf<Function1<Any, Any>>()
        )
    }

    fun getUpdateScopeSetMethod(): KFunction<*> {
        return getClassSubClass("androidx.compose.ui.util.ComponentUpdater", "UpdateScope")
            .declaredFunctions.find {
                it.name == "set"
            }?.apply {
                isAccessible = true
            } ?: throw RuntimeException("ComponentUpdater.UpdateScope set method not found")
    }

    // ######
    fun getWindowSetIconMethod(): KFunction2<Window, Painter?, Unit> {
        return Class.forName("androidx.compose.ui.util.Windows_desktopKt")
            .getDeclaredMethod("setIcon", Window::class.java, Painter::class.java).apply {
                isAccessible = true
            }.kotlinFunction as KFunction2<Window, Painter?, Unit>
    }

    fun getWindowSetUndecoratedSafelyMethod(): KFunction2<Frame, Boolean, Unit> {
        return Class.forName("androidx.compose.ui.util.Windows_desktopKt")
            .getDeclaredMethod("setUndecoratedSafely", Frame::class.java, Boolean::class.java).apply {
                isAccessible = true
            }.kotlinFunction as KFunction2<Frame, Boolean, Unit>
    }

    fun getWindowSetSizeSafelyMethod(): Method {
        return Class.forName("androidx.compose.ui.util.Windows_desktopKt")
            .declaredMethods.find {
                it.name.contains("setSizeSafely")
            }?.apply {
                isAccessible = true
            } ?: throw RuntimeException("Method setSizeSafely not found on class Windows_desktopKt")
    }

    fun getWindowSetPositionSafelyMethod(): Method {
        return Class.forName("androidx.compose.ui.util.Windows_desktopKt")
            .declaredMethods.find {
                it.name.contains("setPositionSafely")
            }?.apply {
                isAccessible = true
            } ?: throw RuntimeException("Method setPositionSafely not found on class Windows_desktopKt")
    }
}
