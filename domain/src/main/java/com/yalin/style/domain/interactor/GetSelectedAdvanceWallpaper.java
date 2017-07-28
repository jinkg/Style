package com.yalin.style.domain.interactor;

import com.yalin.style.domain.AdvanceWallpaper;
import com.yalin.style.domain.repository.SourcesRepository;

import javax.inject.Inject;

/**
 * @author jinyalin
 * @since 2017/7/28.
 */

public class GetSelectedAdvanceWallpaper {
    private SourcesRepository sourcesRepository;

    @Inject
    public GetSelectedAdvanceWallpaper(SourcesRepository sourcesRepository) {
        this.sourcesRepository = sourcesRepository;
    }

    public AdvanceWallpaper getSelected() {
        return sourcesRepository.getWallpaperRepository().getAdvanceWallpaper();
    }
}
