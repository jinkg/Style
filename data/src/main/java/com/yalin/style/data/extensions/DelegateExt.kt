package com.yalin.style.data.extensions

import android.content.Context
import android.content.SharedPreferences
import java.lang.IllegalArgumentException
import kotlin.reflect.KProperty

/**
 * @author jinyalin
 * @since 2017/5/22.
 */
object DelegateExt {
    fun <T> preferences(context: Context, name: String, default: T) =
            Preferences(context, name, default)
}

class Preferences<T>(val context: Context, val name: String, val default: T) {
    companion object {
        val PREFERENCE_NAME = "style_preference"
    }

    val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = findPreference(name, default)

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
            putPreference(name, value)

    @Suppress("UNCHECKED_CAST")
    private fun findPreference(name: String, default: T): T = with(prefs) {
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> throw IllegalArgumentException("This type can be saved into Preferences")
        }

        res as T
    }

    private fun putPreference(name: String, value: T) = with(prefs.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("This type can't be saved into Preferences")
        }.apply()
    }
}