package com.yalin.style.data.utils

import android.content.Context
import android.text.TextUtils
import com.yalin.style.data.log.LogUtil
import java.io.File

/**
 * @author jinyalin
 * @since 2017/8/10.
 */

private val TAG = "NativeFileHelper"
private var nativePath: String? = null

fun getNativeDir(context: Context): File {
    if (TextUtils.isEmpty(nativePath)) {
        val cacheDir = context.cacheDir
        val nativeDir = File(cacheDir.parent, "lib")
        nativeDir.mkdirs()
        nativePath = nativeDir.absolutePath
    }
    return File(nativePath)
}

fun getNativeFileName(componentPath: String, libName: String): String {
    return "plugin_" + getComponentName(componentPath) + "_" + libName + ".so"
}

fun clearNativeFiles(context: Context, componentPath: String) {
    val files = getNativeFiles(context, componentPath)
    for (file in files) {
        file.delete()
    }
}

private fun getNativeFiles(context: Context, componentPath: String): Array<File> {
    val nativePrefix = "plugin_" + getComponentName(componentPath)
    return getNativeDir(context).listFiles({ file -> file.name.contains(nativePrefix) })
}

private fun getComponentName(componentPath: String): String {
    var componentName: String
    try {
        val tmp = componentPath.split("/".toRegex())
        componentName = tmp[tmp.size - 1].split("\\.".toRegex())[0]
    } catch (e: Exception) {
        componentName = componentPath.hashCode().toString()
    }
    LogUtil.D(TAG, "getComponentName for $componentPath result :$componentName")
    return componentName
}