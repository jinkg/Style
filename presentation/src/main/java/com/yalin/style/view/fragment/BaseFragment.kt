package com.yalin.style.view.fragment

import android.app.Fragment
import android.widget.Toast

import com.yalin.style.injection.HasComponent

/**
 * @author jinyalin
 * *
 * @since 2017/4/20.
 */

abstract class BaseFragment : Fragment() {
    /**
     * Shows a [android.widget.Toast] message.

     * @param message An string representing a message to be shown.
     */
    protected fun showToastMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Gets a component for dependency injection by its type.
     */
    protected fun <C> getComponent(componentType: Class<C>): C {
        @Suppress("UNCHECKED_CAST")
        return componentType.cast((activity as HasComponent<C>).component)
    }
}
