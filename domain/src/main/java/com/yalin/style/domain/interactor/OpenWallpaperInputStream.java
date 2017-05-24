package com.yalin.style.domain.interactor;

import com.fernandocejas.arrow.checks.Preconditions;
import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.SourcesRepository;

import java.io.InputStream;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/21.
 */

public class OpenWallpaperInputStream
        extends UseCase<InputStream, OpenWallpaperInputStream.Params> {
    private SourcesRepository sourcesRepository;

    @Inject
    public OpenWallpaperInputStream(ThreadExecutor threadExecutor,
                                    PostExecutionThread postExecutionThread,
                                    SourcesRepository sourcesRepository) {
        super(threadExecutor, postExecutionThread);
        this.sourcesRepository = sourcesRepository;
    }

    @Override
    Observable<InputStream> buildUseCaseObservable(Params params) {
        Preconditions.checkNotNull(params);
        return sourcesRepository.getWallpaperRepository().openInputStream(params.wallpaperId);
    }

    public static final class Params {
        private final String wallpaperId;

        private Params(String wallpaperId) {
            this.wallpaperId = wallpaperId;
        }

        public static Params openInputStream(String wallpaperId) {
            return new Params(wallpaperId);
        }
    }
}
