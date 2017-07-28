package com.yalin.style.view.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import com.yalin.style.R
import com.yalin.style.StyleApplication
import com.yalin.style.model.AdvanceWallpaperItem
import com.yalin.style.presenter.AdvanceSettingPresenter
import com.yalin.style.view.AdvanceSettingView
import kotlinx.android.synthetic.main.activity_advance_setting.*
import javax.inject.Inject

/**
 * @author jinyalin
 * @since 2017/7/28.
 */
class AdvanceSettingActivity : BaseActivity(), AdvanceSettingView {

    @Inject
    lateinit internal var presenter: AdvanceSettingPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StyleApplication.instance.applicationComponent.inject(this)
        setContentView(R.layout.activity_advance_setting)

        setSupportActionBar(appBar)

        initViews()

        presenter.setView(this)
        presenter.initialize()
    }

    private fun initViews() {
        val itemAnimator = DefaultItemAnimator()
        itemAnimator.supportsChangeAnimations = false
        wallpaperList.itemAnimator = itemAnimator

        val gridLayoutManager = GridLayoutManager(this, 1)
        wallpaperList.layoutManager = gridLayoutManager

    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    override fun renderWallpapers(wallpapers: List<AdvanceWallpaperItem>) {

    }

    override fun showLoading() {

    }

    override fun showDownloading() {

    }

    override fun hideLoading() {

    }

    override fun showRetry() {

    }

    override fun hideRetry() {

    }

    override fun showError(message: String) {

    }

    override fun context(): Context {
        return applicationContext
    }
}