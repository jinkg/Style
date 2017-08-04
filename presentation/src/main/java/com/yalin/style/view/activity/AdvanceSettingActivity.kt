package com.yalin.style.view.activity

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
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

    private var loadState = LOAD_STATE_NORMAL

    private var placeHolderDrawable: ColorDrawable? = null
    private var mItemSize = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StyleApplication.instance.applicationComponent.inject(this)
        setContentView(R.layout.activity_advance_setting)

        setSupportActionBar(appBar)

        placeHolderDrawable = ColorDrawable(ContextCompat.getColor(this,
                R.color.gallery_chosen_photo_placeholder))
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

        val gridLayoutManager = GridLayoutManager(this, 1)
        wallpaperList.layoutManager = gridLayoutManager

        btnLoadAdvanceWallpaper.setOnClickListener { presenter.loadAdvanceWallpaper() }
        btnRetry.setOnClickListener { presenter.loadAdvanceWallpaper() }

        wallpaperList.viewTreeObserver
                .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        val width = wallpaperList.width -
                                wallpaperList.paddingStart - wallpaperList.paddingEnd
                        if (width <= 0) {
                            return
                        }

                        // Compute number of columns
                        val maxItemWidth = resources.getDimensionPixelSize(
                                R.dimen.advance_grid_max_item_size)
                        var numColumns = 1
                        while (true) {
                            if (width / numColumns > maxItemWidth) {
                                ++numColumns
                            } else {
                                break
                            }
                        }

                        val spacing = resources.getDimensionPixelSize(
                                R.dimen.gallery_chosen_photo_grid_spacing)
                        mItemSize = (width - spacing * (numColumns - 1)) / numColumns

                        // Complete setup
                        gridLayoutManager.spanCount = numColumns
                        advanceWallpaperAdapter.setHasStableIds(true)
                        wallpaperList.adapter = advanceWallpaperAdapter

                        wallpaperList.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })

        ViewCompat.setOnApplyWindowInsetsListener(wallpaperList) { v, insets ->
            val gridSpacing = resources
                    .getDimensionPixelSize(R.dimen.gallery_chosen_photo_grid_spacing)
            ViewCompat.onApplyWindowInsets(v, insets.replaceSystemWindowInsets(
                    insets.systemWindowInsetLeft + gridSpacing,
                    gridSpacing,
                    insets.systemWindowInsetRight + gridSpacing,
                    insets.systemWindowInsetBottom + insets.systemWindowInsetTop + gridSpacing))

            insets
        }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.advance_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_advance_hint) {
            val dialogBuilder = MaterialDialog.Builder(this)
                    .iconRes(R.drawable.advance_wallpaper_msg)
                    .title(R.string.hint)
                    .content(Html.fromHtml(getString(R.string.advance_hint)))
                    .positiveText(R.string.confirm)

            dialogBuilder.build().show()
            return true
        }
        return super.onOptionsItemSelected(item)
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

    override fun complete() {
        finish()
    }

    override fun wallpaperSelected(wallpaperId: String) {
        wallpapers?.forEach { it ->
            it.isSelected = TextUtils.equals(it.wallpaperId, wallpaperId)
        }
        advanceWallpaperAdapter.notifyDataSetChanged()
    }

    class AdvanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var checkedOverlayView: View = itemView.findViewById(R.id.checked_overlay)
        var thumbnail: ImageView = itemView.findViewById(R.id.thumbnail) as ImageView
        var tvName: TextView = itemView.findViewById(R.id.tvName) as TextView
    }

    private val advanceWallpaperAdapter = object : RecyclerView.Adapter<AdvanceViewHolder>() {
        override fun onBindViewHolder(holder: AdvanceViewHolder, position: Int) {
            wallpapers?.apply {
                val item = this[position]
                holder.thumbnail.layoutParams.width = mItemSize
                holder.thumbnail.layoutParams.height = mItemSize
                Glide.with(this@AdvanceSettingActivity)
                        .load(item.iconUrl)
                        .override(mItemSize, mItemSize)
                        .placeholder(placeHolderDrawable)
                        .into(holder.thumbnail)

                if (item.isSelected) {
                    holder.checkedOverlayView.visibility = View.VISIBLE
                } else {
                    holder.checkedOverlayView.visibility = View.GONE
                }

                holder.tvName.text = item.name
            }
        }

        override fun getItemCount(): Int {
            return if (wallpapers == null) 0 else wallpapers!!.size
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AdvanceViewHolder {
            val view = LayoutInflater.from(this@AdvanceSettingActivity)
                    .inflate(R.layout.advance_chosen_wallpaper_item, parent, false)

            val vh = AdvanceViewHolder(view)
            view.setOnClickListener {
                val item = wallpapers!![vh.adapterPosition]
                presenter.selectAdvanceWallpaper(item.wallpaperId, false)
            }

            return vh
        }

    }
}