package com.yalin.style.extensions

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author jinyalin
 * @since 2017/5/8.
 */
object DelegatesExt {
    fun <T> notNullSingleValue():
            ReadWriteProperty<Any?, T> = NotNullSingleValueVar()
}

private class NotNullSingleValueVar<T> : ReadWriteProperty<Any?, T> {
    private var value: T? = null
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = if (this.value == null) value
        else throw IllegalStateException("${property.name} already initialized")
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("${property.name} " +
                "not initialized")
    }
}