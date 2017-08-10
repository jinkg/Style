package com.yalin.style.engine.component

import android.content.Context
import android.os.Build
import android.util.Log
import com.yalin.style.data.utils.getNativeDir
import com.yalin.style.data.utils.getNativeFileName

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import dalvik.system.DexClassLoader

/**
 * @author jinyalin
 * *
 * @since 2017/8/9.
 */

class StyleClassLoader(private val context: Context,
                       private val dexPath: String,
                       optimizedDirectory: String,
                       librarySearchPath: String, parent: ClassLoader) : DexClassLoader(dexPath, optimizedDirectory, librarySearchPath, parent) {

    override fun findLibrary(name: String): String? {
        val soName = getNativeFileName(dexPath, name)
        maybeCopyNativeLib(soName)
        val targetFile = File(getNativeDir(context), soName)
        return if (targetFile.exists()) targetFile.absolutePath else null
    }

    private fun maybeCopyNativeLib(libName: String) {
        try {
            val soFile = File(getNativeDir(context), libName)
            if (soFile.exists()) {
                return
            }

            val cpuArch: String
            if (Build.VERSION.SDK_INT >= 21) {
                cpuArch = Build.SUPPORTED_ABIS[0]
            } else {
                cpuArch = Build.CPU_ABI
            }
            var findSo = false

            val zipfile = ZipFile(dexPath)
            var entry: ZipEntry
            var e: Enumeration<*> = zipfile.entries()
            while (e.hasMoreElements()) {
                entry = e.nextElement() as ZipEntry
                if (entry.isDirectory)
                    continue
                if (entry.name.endsWith(".so") && entry.name.contains("lib/" + cpuArch)) {
                    findSo = true
                    break
                }
            }
            e = zipfile.entries()
            while (e.hasMoreElements()) {
                entry = e.nextElement()
                if (entry.isDirectory || !entry.name.endsWith(".so"))
                    continue
                if (findSo && entry.name.contains("lib/" + cpuArch)
                        || !findSo && entry.name.contains("lib/armeabi/")) {
                    val libFile = File(getNativeDir(context).getAbsolutePath()
                            + File.separator + libName)
                    if (libFile.exists()) {
                        // check version
                    }
                    val fos = FileOutputStream(libFile)
                    Log.d(TAG, "copy so " + entry.name + " of " + cpuArch)
                    copySo(zipfile.getInputStream(entry), fos)
                    break
                }

            }

            zipfile.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun copySo(input: InputStream, output: OutputStream) {
        val bufferedInput = BufferedInputStream(input)
        val bufferedOutput = BufferedOutputStream(output)
        var count: Int
        val data = ByteArray(8192)
        count = bufferedInput.read(data, 0, 8192)
        while (count != -1) {
            bufferedOutput.write(data, 0, count)
            count = bufferedInput.read(data, 0, 8192)
        }
        bufferedOutput.flush()
        bufferedOutput.close()
        output.close()
        bufferedInput.close()
        input.close()
    }

    companion object {
        private val TAG = "StyleClassLoader"
    }
}
