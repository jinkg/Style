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
import javax.inject.Inject

/**
 * @author jinyalin
 * @since 2017/7/28.
 */
class AdvanceSettingActivity : BaseActivity(), AdvanceSettingView {

    @Inject
    lateinit internal var presenter: AdvanceSettingPresenter

    var wallpapers: List<AdvanceWallpaperItem>? = null

    var imageLoader: ImageLoader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StyleApplication.instance.applicationComponent.inject(this)
        setContentView(R.layout.activity_advance_setting)

        setSupportActionBar(appBar)

        initViews()

        imageLoader = ImageLoader(this)
        presenter.setView(this)
        presenter.initialize()
    }

    private fun initViews() {
        val itemAnimator = DefaultItemAnimator()
        itemAnimator.supportsChangeAnimations = false
        wallpaperList.itemAnimator = itemAnimator

        val gridLayoutManager = GridLayoutManager(this, 2)
        wallpaperList.layoutManager = gridLayoutManager

        wallpaperList.adapter = advanceWallpaperAdapter

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
        this.wallpapers = wallpapers
        advanceWallpaperAdapter.notifyDataSetChanged()
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

    override fun showEmpty() {

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