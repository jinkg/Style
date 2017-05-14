package com.yalin.style

import com.yalin.style.domain.executor.PostExecutionThread

import javax.inject.Inject
import javax.inject.Singleton

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * @author jinyalin
 * *
 * @since 2017/4/18.
 */
@Singleton
class UIThread @Inject
constructor() : PostExecutionThread {

    override fun getScheduler(): Scheduler {
        return AndroidSchedulers.mainThread()
    }
}