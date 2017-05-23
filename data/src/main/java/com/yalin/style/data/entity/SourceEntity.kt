package com.yalin.style.data.entity


/**
 * @author jinyalin
 * *
 * @since 2017/5/23.
 */

class SourceEntity(var id: Int) {
    var title: String? = null
    var description: String? = null
    var iconId: Int = 0
    var isSelected: Boolean = false
    var isHasSetting: Boolean = false
    var color: Int = 0
}
