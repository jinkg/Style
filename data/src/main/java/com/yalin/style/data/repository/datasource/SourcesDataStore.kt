package com.yalin.style.data.repository.datasource

import com.yalin.style.data.entity.SourceEntity
import io.reactivex.Observable

/**
 * @author jinyalin
 * @since 2017/5/23.
 */
interface SourcesDataStore {

    fun getSources(): Observable<List<SourceEntity>>

    fun selectSource(sourceId: Int, tempSelect: Boolean): Observable<Boolean>

    fun getUsedSourceId(): Int
}