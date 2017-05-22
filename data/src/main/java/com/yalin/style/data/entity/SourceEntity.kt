package com.yalin.style.data.entity

/**
 * @author jinyalin
 * @since 2017/5/22.
 */
class SourceEntity(val id: Int) {
    var title: String? = null
    var description: String? = null
    var iconId: Int = 0
    var selected: Boolean = false
    var hasSetting: Boolean = false
    var color: Int = 0
}