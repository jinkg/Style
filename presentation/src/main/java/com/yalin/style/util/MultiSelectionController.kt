package com.yalin.style.util

import android.os.Bundle
import android.os.Parcelable
import java.util.HashSet

/**
 * @author jinyalin
 * @since 2017/5/25.
 */
class MultiSelectionController<T : Parcelable>(val stateKey: String) {
    private val mSelection = HashSet<T>()
    private val DUMMY_CALLBACKS: Callbacks = object : Callbacks {
        override fun onSelectionChanged(restored: Boolean, fromUser: Boolean) {}
    }
    private var mCallbacks: Callbacks? = DUMMY_CALLBACKS

    @Suppress("UNCHECKED_CAST")
    fun restoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            mSelection.clear()
            val selection = savedInstanceState.getParcelableArray(stateKey)
            if (selection != null && selection.isNotEmpty()) {
                selection.mapTo(mSelection) { it as T }
            }
        }

        mCallbacks?.onSelectionChanged(true, false)
    }

    fun saveInstanceState(outBundle: Bundle) {
        val selection = arrayOfNulls<Parcelable>(mSelection.size)
        for ((i, item) in mSelection.withIndex()) {
            selection[i] = item
        }

        outBundle.putParcelableArray(stateKey, selection)
    }

    fun setCallbacks(callbacks: Callbacks) {
        mCallbacks = callbacks
        if (mCallbacks == null) {
            mCallbacks = DUMMY_CALLBACKS
        }
    }

    fun getSelection(): Set<T> {
        return HashSet(mSelection)
    }

    fun getSelectedCount(): Int {
        return mSelection.size
    }

    fun isSelecting(): Boolean {
        return mSelection.size > 0
    }

    fun toggle(item: T, fromUser: Boolean) {
        if (mSelection.contains(item)) {
            mSelection.remove(item)
        } else {
            mSelection.add(item)
        }

        mCallbacks?.onSelectionChanged(false, fromUser)
    }

    fun reset(fromUser: Boolean) {
        mSelection.clear()
        mCallbacks?.onSelectionChanged(false, fromUser)
    }

    fun isSelected(item: T): Boolean {
        return mSelection.contains(item)
    }

    interface Callbacks {
        fun onSelectionChanged(restored: Boolean, fromUser: Boolean)
    }
}