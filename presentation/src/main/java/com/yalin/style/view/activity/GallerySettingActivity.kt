package com.yalin.style.view.activity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
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
import com.yalin.style.R
import com.yalin.style.StyleApplication
import com.yalin.style.domain.GalleryWallpaper
import com.yalin.style.domain.interactor.AddGalleryWallpaper
import com.yalin.style.domain.interactor.DefaultObserver
import com.yalin.style.domain.interactor.GetGalleryWallpaper
import kotlinx.android.synthetic.main.activity_gallery.*
import org.jetbrains.anko.toast
import java.util.ArrayList
import java.util.HashSet
import javax.inject.Inject

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

    private var mUpdatePosition = -1

    @Inject
    lateinit internal var addGalleryWallpaperUseCase: AddGalleryWallpaper

    @Inject
    lateinit internal var getGalleryWallpaperUseCase: GetGalleryWallpaper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_gallery)

        StyleApplication.instance.applicationComponent.inject(this)

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
                tryUpdateSelection(false)
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
            requestPhotos()
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

        refreshGalleryWallpaper()
    }

    override fun onResume() {
        super.onResume()
        onDataSetChanged()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_STORAGE_PERMISSION) {
            return
        }
        onDataSetChanged()
    }

    private fun refreshGalleryWallpaper() {
        getGalleryWallpaperUseCase.execute(object : DefaultObserver<Set<GalleryWallpaper>>() {
            override fun onNext(success: Set<GalleryWallpaper>) {
                super.onNext(success)
            }
        }, null)
    }

    private fun tryUpdateSelection(allowAnimate: Boolean) {
        if (mUpdatePosition >= 0) {
            mChosenPhotosAdapter.notifyItemChanged(mUpdatePosition)
            mUpdatePosition = -1
        } else {
            mChosenPhotosAdapter.notifyDataSetChanged()
        }

//        val selectedCount = mMultiSelectionController.getSelectedCount()
        val selectedCount = 0
        val toolbarVisible = selectedCount > 0
        var showForceNow = selectedCount == 1
        if (showForceNow) {
            // Double check to make sure we can force a URI for the selected URI
//            val selectedUri = mMultiSelectionController.getSelection().iterator().next()
//            val data = contentResolver.query(selectedUri,
//                    arrayOf<String>(GalleryContract.ChosenPhotos.COLUMN_NAME_IS_TREE_URI, GalleryContract.ChosenPhotos.COLUMN_NAME_URI), null, null, null)
//            if (data != null && data.moveToNext()) {
//                val isTreeUri = data.getInt(0) != 0
//                // Only show the force now icon if it isn't a tree URI or there is at least one image in the tree
//                showForceNow = !isTreeUri || !getImagesFromTreeUri(Uri.parse(data.getString(1)), 1).isEmpty()
//            }
//            data?.close()
        }
//        selectionToolbar.menu.findItem(R.id.action_force_now).isVisible = showForceNow

//        var previouslyVisible: Boolean? = selectionToolbarContainer.getTag(0xDEADBEEF.toInt()) as Boolean
        var previouslyVisible = false

        if (previouslyVisible !== toolbarVisible) {
            selectionToolbarContainer.setTag(0xDEADBEEF.toInt(), toolbarVisible)

            val duration = if (allowAnimate)
                resources.getInteger(android.R.integer.config_shortAnimTime)
            else
                0
            if (toolbarVisible) {
                selectionToolbarContainer.visibility = View.VISIBLE
                selectionToolbarContainer.translationY = (-selectionToolbarContainer.height).toFloat()
                selectionToolbarContainer.animate()
                        .translationY(0f)
                        .setDuration(duration.toLong())
                        .withEndAction(null)

                if (addToolbar.visibility == View.VISIBLE) {
                    hideAddToolbar(false)
                } else {
                    addFab.animate()
                            .scaleX(0f)
                            .scaleY(0f)
                            .setDuration(duration.toLong())
                            .withEndAction({ addFab.visibility = View.INVISIBLE })
                }
            } else {
                selectionToolbarContainer.animate()
                        .translationY((-selectionToolbarContainer.height).toFloat())
                        .setDuration(duration.toLong())
                        .withEndAction { selectionToolbarContainer.visibility = View.INVISIBLE }

                addFab.visibility = View.VISIBLE
                addFab.animate()
                        .scaleY(1f)
                        .scaleX(1f)
                        .setDuration(duration.toLong())
                        .withEndAction(null)
            }
        }

        if (toolbarVisible) {
            var title = Integer.toString(selectedCount)
            if (selectedCount == 1) {
                // If they've selected a tree URI, show the DISPLAY_NAME instead of just '1'
//                val selectedUri = mMultiSelectionController.getSelection().iterator().next()
//                val selectedUri = mMultiSelectionController.getSelection().iterator().next()
//                val data = contentResolver.query(selectedUri,
//                        arrayOf<String>(GalleryContract.ChosenPhotos.COLUMN_NAME_IS_TREE_URI, GalleryContract.ChosenPhotos.COLUMN_NAME_URI), null, null, null)
//                if (data != null && data.moveToNext()) {
//                    val isTreeUri = data.getInt(0) != 0
//                    if (isTreeUri) {
//                        val displayName = getDisplayNameForTreeUri(Uri.parse(data.getString(1)))
//                        if (!TextUtils.isEmpty(displayName)) {
//                            title = displayName
//                        }
//                    }
//                }
//                data?.close()
            }
            selectionToolbar.title = title
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        super.onActivityResult(requestCode, resultCode, result)
        if (requestCode != REQUEST_CHOOSE_PHOTOS && requestCode != REQUEST_CHOOSE_FOLDER) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!addToolbar.isAttachedToWindow) {
                // Can't animate detached Views
                addToolbar.visibility = View.INVISIBLE
                addFab.visibility = View.VISIBLE
            } else {
                hideAddToolbar(true)
            }
        }

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (result == null) {
            return
        }

        if (requestCode == REQUEST_CHOOSE_FOLDER) {
            val preferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            preferences.edit().putBoolean(SHOW_INTERNAL_STORAGE_MESSAGE, false).apply()
        }

        // Add chosen items
        val uris = HashSet<String>()
        if (result.data != null) {
            uris.add(result.data.toString())
        }
        // When selecting multiple images, "Photos" returns the first URI in getData and all URIs
        // in getClipData.
        val clipData = result.clipData
        if (clipData != null) {
            val count = clipData.itemCount
            for (i in 0..count - 1) {
                val uri = clipData.getItemAt(i).uri
                if (uri != null) {
                    uris.add(uri.toString())
                }
            }
        }

        if (uris.isEmpty()) {
            // Nothing to do, so we can avoid posting the runnable at all
            return
        }

        Set<GalleryWallpaper>
        for()

        addGalleryWallpaperUseCase.execute(
                AddCustomWallpaperObserver(),
                AddGalleryWallpaper.Params.addCustomWallpaperUris(uris))
        // Update chosen URIs
//        runOnHandlerThread(Runnable {
//            val operations = ArrayList<ContentProviderOperation>()
//            for (uri in uris) {
//                val values = ContentValues()
//                values.put(GalleryContract.ChosenPhotos.COLUMN_NAME_URI, uri.toString())
//                operations.add(ContentProviderOperation.newInsert(GalleryContract.ChosenPhotos.CONTENT_URI)
//                        .withValues(values).build())
//            }
//            try {
//                contentResolver.applyBatch(GalleryContract.AUTHORITY, operations)
//            } catch (e: RemoteException) {
//                Log.e(TAG, "Error writing uris to ContentProvider", e)
//            } catch (e: OperationApplicationException) {
//                Log.e(TAG, "Error writing uris to ContentProvider", e)
//            }
//        })
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

    private fun onDataSetChanged() {
//        if (mChosenUris != null && mChosenUris.getCount() > 0) {
//            emptyView.visibility = View.GONE
//            // We have at least one image, so consider the Gallery source properly setup
//            setResult(Activity.RESULT_OK)
//        } else {
        // No chosen images, show the empty View
        empty.visibility = View.VISIBLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, we can show the random camera photos image
            emptyAnimator.displayedChild = 0
            emptyDescription.setText(R.string.gallery_empty)
            setResult(Activity.RESULT_OK)
        } else {
            // We have no images until they enable the permission
            setResult(Activity.RESULT_CANCELED)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // We should show rationale on why they should enable the storage permission and
                // random camera photos
                emptyAnimator.displayedChild = 1
                emptyDescription.setText(R.string.gallery_permission_rationale)
            } else {
                // The user has permanently denied the storage permission. Give them a link to app settings
                emptyAnimator.displayedChild = 2
                emptyDescription.setText(R.string.gallery_denied_explanation)
            }
        }
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

    private inner class AddCustomWallpaperObserver : DefaultObserver<Boolean>() {
        override fun onNext(success: Boolean) {
            super.onNext(success)
            if (success) {
                refreshGalleryWallpaper()
            }
        }
    }
}