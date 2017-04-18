package com.yalin.style.domain.interactor;

import com.yalin.style.domain.WallPaper;
import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.WallPaperRepository;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public class GetWallPaper extends UseCase<WallPaper, Void> {
    private WallPaperRepository wallPaperRepository;

    public GetWallPaper(ThreadExecutor threadExecutor,
                        PostExecutionThread postExecutionThread,
                        WallPaperRepository wallPaperRepository) {
        super(threadExecutor, postExecutionThread);
        this.wallPaperRepository = wallPaperRepository;
    }

    @Override
    Observable<WallPaper> buildUseCaseObservable(Void aVoid) {
        return wallPaperRepository.getWallPaper();
    }
}
