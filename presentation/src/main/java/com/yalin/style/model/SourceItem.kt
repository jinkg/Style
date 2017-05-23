package com.yalin.style.model

import android.view.View

/**
 * @author jinyalin
 * @since 2017/5/22.
 */
class SourceItem {
    var id: Int = 0
    var title: String? = null
    var description: String? = null
    var iconId: Int = 0
    var selected: Boolean = false
    var hasSetting: Boolean = false
    var color: Int = 0

    var rootView: View? = null
}