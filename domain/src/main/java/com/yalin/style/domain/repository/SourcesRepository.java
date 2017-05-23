package com.yalin.style.domain.repository;

import com.yalin.style.domain.Source;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/5/23.
 */

public interface SourcesRepository {

    Observable<List<Source>> getSources();

    Observable<Boolean> selectSource(int sourceId);

}
