package com.yalin.style.domain.interactor;

import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.SourcesRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/6/9.
 */

public class ForceNow extends UseCase<Boolean, ForceNow.Params> {
    private SourcesRepository sourcesRepository;

    @Inject
    public ForceNow(ThreadExecutor threadExecutor,
                    PostExecutionThread postExecutionThread,
                    SourcesRepository sourcesRepository) {
        super(threadExecutor, postExecutionThread);
        this.sourcesRepository = sourcesRepository;
    }

    @Override
    Observable<Boolean> buildUseCaseObservable(Params params) {
        return sourcesRepository.getWallpaperRepository().foreNow(params.galleryWallpaperUri);
    }

    public static final class Params {
        private final String galleryWallpaperUri;

        private Params(String galleryWallpaperUri) {
            this.galleryWallpaperUri = galleryWallpaperUri;
        }

        public static Params fromUri(String galleryWallpaperUri) {
            return new Params(galleryWallpaperUri);
        }
    }
}
