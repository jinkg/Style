package com.yalin.style.model

import com.yalin.style.domain.repository.SourcesRepository

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
        get() = id == SourcesRepository.SOURCE_ID_CUSTOM
}