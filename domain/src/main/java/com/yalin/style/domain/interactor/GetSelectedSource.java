package com.yalin.style.domain.interactor;

import com.yalin.style.domain.repository.SourcesRepository;

import javax.inject.Inject;

/**
 * @author jinyalin
 * @since 2017/7/27.
 */

public class GetSelectedSource {
    private SourcesRepository sourcesRepository;

    @Inject
    public GetSelectedSource(SourcesRepository sourcesRepository) {
        this.sourcesRepository = sourcesRepository;
    }

    public int getSelectedSourceId() {
        return sourcesRepository.getSelectedSource();
    }
}
