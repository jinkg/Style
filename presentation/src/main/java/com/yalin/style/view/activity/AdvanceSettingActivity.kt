package com.yalin.style.view.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.yalin.style.R
import com.yalin.style.StyleApplication
import com.yalin.style.model.AdvanceWallpaperItem
import com.yalin.style.presenter.AdvanceSettingPresenter
import com.yalin.style.util.ImageLoader
import com.yalin.style.view.AdvanceSettingView
import kotlinx.android.synthetic.main.activity_advance_setting.*
import org.jetbrains.anko.toast
import javax.inject.Inject

/**
 * @author jinyalin
 * @since 2017/7/28.
 */
class AdvanceSettingActivity : BaseActivity(), AdvanceSettingView {
    companion object {
        val LOAD_STATE = "load_state"

        val LOAD_STATE_NORMAL = 0
        val LOAD_STATE_LOADING = 1
        val LOAD_STATE_RETRY = 2
    }

    @Inject
    lateinit internal var presenter: AdvanceSettingPresenter

    var wallpapers: List<AdvanceWallpaperItem>? = null

    var imageLoader: ImageLoader? = null

    var loadState = LOAD_STATE_NORMAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StyleApplication.instance.applicationComponent.inject(this)
        setContentView(R.layout.activity_advance_setting)

        setSupportActionBar(appBar)

        initViews()

        imageLoader = ImageLoader(this)
        presenter.setView(this)

        if (savedInstanceState != null) {
            loadState = savedInstanceState.getInt(LOAD_STATE)
        }

        handleState()
    }

    private fun handleState() {
        if (loadState == LOAD_STATE_NORMAL) {
            presenter.initialize()
        } else if (loadState == LOAD_STATE_LOADING) {
            presenter.loadAdvanceWallpaper()
        } else if (loadState == LOAD_STATE_RETRY) {
            showRetry()
        }
    }

    private fun initViews() {
        val itemAnimator = DefaultItemAnimator()
        itemAnimator.supportsChangeAnimations = false
        wallpaperList.itemAnimator = itemAnimator

        val gridLayoutManager = GridLayoutManager(this, 2)
        wallpaperList.layoutManager = gridLayoutManager

        wallpaperList.adapter = advanceWallpaperAdapter

        btnLoadAdvanceWallpaper.setOnClickListener { presenter.loadAdvanceWallpaper() }
        btnRetry.setOnClickListener { presenter.loadAdvanceWallpaper() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(LOAD_STATE, loadState)
        super.onSaveInstanceState(outState)
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
        loadState = LOAD_STATE_NORMAL

        this.wallpapers = wallpapers

        wallpaperList.visibility = View.VISIBLE
        empty.visibility = View.GONE
        loading.visibility = View.GONE
        retry.visibility = View.GONE
        advanceWallpaperAdapter.notifyDataSetChanged()
    }

    override fun showLoading() {
        loadState = LOAD_STATE_LOADING

        wallpaperList.visibility = View.GONE
        empty.visibility = View.GONE
        loading.visibility = View.VISIBLE
        retry.visibility = View.GONE
    }

    override fun hideLoading() {

    }

    override fun showRetry() {
        loadState = LOAD_STATE_RETRY

        wallpaperList.visibility = View.GONE
        empty.visibility = View.GONE
        loading.visibility = View.GONE
        retry.visibility = View.VISIBLE
    }

    override fun hideRetry() {

    }

    override fun showError(message: String) {
        toast(message)
    }

    override fun showEmpty() {
        wallpaperList.visibility = View.GONE
        empty.visibility = View.VISIBLE
        loading.visibility = View.GONE
        retry.visibility = View.GONE
    }

    override fun context(): Context {
        return applicationContext
    }

    class AdvanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var checkedOverlayView: View = itemView.findViewById(R.id.checked_overlay)
        var thumbnail: ImageView = itemView.findViewById(R.id.thumbnail) as ImageView
    }

    private val advanceWallpaperAdapter = object : RecyclerView.Adapter<AdvanceViewHolder>() {
        override fun onBindViewHolder(holder: AdvanceViewHolder, position: Int) {
            wallpapers?.apply {
                val item = this[position]
                imageLoader?.loadImage(item.iconUrl, holder.thumbnail,
                        null, getDrawable(R.drawable.logo))
            }
        }

        override fun getItemCount(): Int {
            return if (wallpapers == null) 0 else wallpapers!!.size
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AdvanceViewHolder {
            val view = LayoutInflater.from(this@AdvanceSettingActivity)
                    .inflate(R.layout.advance_chosen_wallpaper_item, parent, false)

            return AdvanceViewHolder(view)
        }

    }
}