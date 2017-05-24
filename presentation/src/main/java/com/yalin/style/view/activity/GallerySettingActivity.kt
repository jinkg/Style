package com.yalin.style.view.activity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import com.yalin.style.R
import kotlinx.android.synthetic.main.activity_gallery.*
import org.jetbrains.anko.toast
import java.util.ArrayList

/**
 * @author jinyalin
 * @since 2017/5/24.
 */
class GallerySettingActivity : BaseActivity() {
    companion object {
        private val TAG = "GallerySettingsActivity"
        private val DOCUMENTS_UI_PACKAGE_NAME = "com.android.documentsui"
        private val SHARED_PREF_NAME = "GallerySettingsActivity"
        private val SHOW_INTERNAL_STORAGE_MESSAGE = "show_internal_storage_message"
        private val REQUEST_CHOOSE_PHOTOS = 1
        private val REQUEST_CHOOSE_FOLDER = 2
        private val REQUEST_STORAGE_PERMISSION = 3
    }

    private var mPlaceholderDrawable: ColorDrawable? = null
    private var mPlaceholderSmallDrawable: ColorDrawable? = null

    private var mItemSize = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_gallery)
        setSupportActionBar(appBar)

        mPlaceholderDrawable = ColorDrawable(ContextCompat.getColor(this,
                R.color.gallery_chosen_photo_placeholder))
        mPlaceholderSmallDrawable = ColorDrawable(ContextCompat.getColor(this,
                R.color.gallery_chosen_photo_placeholder))

        val itemAnimator = DefaultItemAnimator()
        itemAnimator.supportsChangeAnimations = false
        photoGrid.itemAnimator = itemAnimator

        val gridLayoutManager = GridLayoutManager(this, 1)
        photoGrid.layoutManager = gridLayoutManager

        val vto = photoGrid.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val width = photoGrid.width - photoGrid.paddingStart - photoGrid.paddingEnd
                if (width <= 0) {
                    return
                }

                // Compute number of columns
                val maxItemWidth = resources.getDimensionPixelSize(
                        R.dimen.gallery_chosen_photo_grid_max_item_size)
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
                mChosenPhotosAdapter.setHasStableIds(true)
                photoGrid.adapter = mChosenPhotosAdapter

                photoGrid.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                tryUpdateSelection(false)
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(photoGrid) { v, insets ->
            val gridSpacing = resources
                    .getDimensionPixelSize(R.dimen.gallery_chosen_photo_grid_spacing)
            ViewCompat.onApplyWindowInsets(v, insets.replaceSystemWindowInsets(
                    insets.systemWindowInsetLeft + gridSpacing,
                    gridSpacing,
                    insets.systemWindowInsetRight + gridSpacing,
                    insets.systemWindowInsetBottom + insets.systemWindowInsetTop + gridSpacing +
                            resources.getDimensionPixelSize(R.dimen.gallery_fab_space)))

            insets
        }

        btnGalleryEnableRandom.setOnClickListener {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_PERMISSION)
        }

        btnGalleryEditPermissionSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        addFab.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // On Lollipop and higher, we show the add toolbar to allow users to add either
                // individual photos or a whole directory
                showAddToolbar()
            } else {
                requestPhotos()
            }
        }

        addPhotos.setOnClickListener {
            requestPhotos();
        }

        addFolder.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            try {
                startActivityForResult(intent, REQUEST_CHOOSE_FOLDER)
                val preferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                if (preferences.getBoolean(SHOW_INTERNAL_STORAGE_MESSAGE, true)) {
                    toast(R.string.gallery_internal_storage_message)
                }
            } catch (e: ActivityNotFoundException) {
                Snackbar.make(photoGrid, R.string.gallery_add_folder_error,
                        Snackbar.LENGTH_LONG).show()
                hideAddToolbar(true)
            }
        }
    }

    private fun requestPhotos() {
        // Use ACTION_OPEN_DOCUMENT by default for adding photos.
        // This allows us to use persistent URI permissions to access the underlying photos
        // meaning we don't need to use additional storage space and will pull in edits automatically
        // in addition to syncing deletions.
        // (There's a separate 'Import photos' option which uses ACTION_GET_CONTENT to support legacy apps)
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        try {
            startActivityForResult(intent, REQUEST_CHOOSE_PHOTOS)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(photoGrid, R.string.gallery_add_photos_error,
                    Snackbar.LENGTH_LONG).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                hideAddToolbar(true)
            }
        }

    }

    override fun onBackPressed() {
//        if (mMultiSelectionController.getSelectedCount() > 0) {
//            mMultiSelectionController.reset(true)
//        } else if (mAddToolbar.getVisibility() == View.VISIBLE) {
//            hideAddToolbar(true)
//        } else {
//            super.onBackPressed()
//        }

        if (addToolbar.visibility == View.VISIBLE) {
            hideAddToolbar(true)
        } else {
            super.onBackPressed()
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showAddToolbar() {
        // Divide by two since we're doing two animations but we want the total time to the short animation time
        val duration = resources.getInteger(android.R.integer.config_shortAnimTime) / 2
        // Hide the add button
        addFab.animate()
                .scaleX(0f)
                .scaleY(0f)
                .translationY(resources.getDimension(R.dimen.gallery_fab_margin))
                .setDuration(duration.toLong())
                .withEndAction {
                    addFab.visibility = View.INVISIBLE
                    // Then show the toolbar
                    addToolbar.visibility = View.VISIBLE
                    ViewAnimationUtils.createCircularReveal(
                            addToolbar,
                            addToolbar.width / 2,
                            addToolbar.height / 2,
                            0f,
                            (addToolbar.width / 2).toFloat())
                            .setDuration(duration.toLong())
                            .start()
                }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun hideAddToolbar(showAddButton: Boolean) {
        // Divide by two since we're doing two animations but we want the total time to the short animation time
        val duration = resources.getInteger(android.R.integer.config_shortAnimTime) / 2
        // Hide the toolbar
        val hideAnimator = ViewAnimationUtils.createCircularReveal(
                addToolbar,
                addToolbar.width / 2,
                addToolbar.height / 2,
                (addToolbar.width / 2).toFloat(),
                0f).setDuration((if (showAddButton) duration else duration * 2).toLong())
        hideAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                addToolbar.visibility = View.INVISIBLE
                if (showAddButton) {
                    addFab.visibility = View.VISIBLE
                    addFab.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationY(0f).duration = duration.toLong()
                } else {
                    // Just reset the translationY
                    addFab.translationY = 0f
                }
            }
        })
        hideAnimator.start()
    }

    open class CheckableViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        var mRootView: View = root
        var mCheckedOverlayView: View = root.findViewById(R.id.checked_overlay)

    }

    internal class PhotoViewHolder(root: View) : CheckableViewHolder(root) {
        val mThumbView: ImageView = root.findViewById(R.id.thumbnail) as ImageView

    }

    internal class TreeViewHolder(root: View) : CheckableViewHolder(root) {
        val mThumbViews: MutableList<ImageView> = ArrayList()

        init {
            mThumbViews.add(root.findViewById(R.id.thumbnail1) as ImageView)
            mThumbViews.add(root.findViewById(R.id.thumbnail2) as ImageView)
            mThumbViews.add(root.findViewById(R.id.thumbnail3) as ImageView)
            mThumbViews.add(root.findViewById(R.id.thumbnail4) as ImageView)
        }
    }

    private val mChosenPhotosAdapter = object : RecyclerView.Adapter<CheckableViewHolder>() {
        override fun getItemViewType(position: Int): Int {
            return 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckableViewHolder {
            val view = LayoutInflater.from(this@GallerySettingActivity)
                    .inflate(R.layout.gallery_chosen_photo_tree_item, parent, false)
            return TreeViewHolder(view)
        }

        override fun onBindViewHolder(vh: CheckableViewHolder, position: Int) {

        }

        private fun maxDistanceToCorner(x: Int, y: Int, left: Int, top: Int, right: Int, bottom: Int): Float {
            var maxDistance = 0f
            maxDistance = Math.max(maxDistance,
                    Math.hypot((x - left).toDouble(), (y - top).toDouble()).toFloat())
            maxDistance = Math.max(maxDistance,
                    Math.hypot((x - right).toDouble(), (y - top).toDouble()).toFloat())
            maxDistance = Math.max(maxDistance,
                    Math.hypot((x - left).toDouble(), (y - bottom).toDouble()).toFloat())
            maxDistance = Math.max(maxDistance,
                    Math.hypot((x - right).toDouble(), (y - bottom).toDouble()).toFloat())
            return maxDistance
        }

        override fun getItemCount(): Int {
            return 0
        }

        override fun getItemId(position: Int): Long {
            return 0
        }
    }
}