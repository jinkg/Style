package com.yalin.style.data.cache

import android.content.Context
import com.yalin.style.data.entity.SourceEntity
import io.reactivex.Observable

/**
 * @author jinyalin
 * @since 2017/5/22.
 */
interface SourcesCache {
    fun getSources(ctx: Context): Observable<List<SourceEntity>>

    fun selectSource(selectSourceId: Int, tempSelect: Boolean): Boolean

    fun getUsedSourceId(): Int
}