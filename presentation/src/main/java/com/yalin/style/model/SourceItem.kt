package com.yalin.style.model

import com.yalin.style.data.cache.SourcesCacheImpl

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

    val needPermission: Boolean
        get() = id == SourcesCacheImpl.SOURCE_ID_CUSTOM
}