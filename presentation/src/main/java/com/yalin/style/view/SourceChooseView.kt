package com.yalin.style.view

import com.yalin.style.model.SourceItem

/**
 * @author jinyalin
 * @since 2017/5/23.
 */
interface SourceChooseView : LoadingDataView {
    fun renderSources(sources: List<SourceItem>)

    fun sourceSelected(sources: List<SourceItem>, selectedItem: SourceItem)

    fun executeDelay(runnable: Runnable, ms: Long)
}