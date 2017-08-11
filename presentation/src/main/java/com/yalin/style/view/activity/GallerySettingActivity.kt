package com.yalin.style.view.activity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.Activity
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.SparseIntArray
import android.view.*
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.yalin.style.R
import com.yalin.style.StyleApplication
import com.yalin.style.analytics.Analytics
import com.yalin.style.analytics.Event
import com.yalin.style.data.utils.getDisplayNameForTreeUri
import com.yalin.style.data.utils.getImagesFromTreeUri
import com.yalin.style.model.GalleryWallpaperItem
import com.yalin.style.presenter.GallerySettingPresenter
import com.yalin.style.util.MultiSelectionController
import com.yalin.style.util.maybeAttachAd
import com.yalin.style.view.GallerySettingView
import kotlinx.android.synthetic.main.activity_gallery_setting.*
import org.jetbrains.anko.toast
import java.util.*
import javax.inject.Inject

/**
 * @author jinyalin
 * @since 2017/5/24.
 */
class GallerySettingActivity : BaseActivity(), GallerySettingView {

    companion object {
        private val REQUEST_CHOOSE_PHOTOS = 1
        private val REQUEST_CHOOSE_FOLDER = 2
        private val REQUEST_STORAGE_PERMISSION = 3

        private val SHARED_PREF_NAME = "GallerySettingsActivity"
        private val SHOW_INTERNAL_STORAGE_MESSAGE = "show_internal_storage_message"

        private val ITEM_TYPE_URI = 0
        private val ITEM_TYPE_TREE = 1

        private val STATE_SELECTION = "selection"
    }

    @Inject
    lateinit internal var presenter: GallerySettingPresenter

    private var mPlaceholderDrawable: ColorDrawable? = null
    private var mPlaceholderSmallDrawable: ColorDrawable? = null

    private var mItemSize = 10

    private val mMultiSelectionController =
            MultiSelectionController<GalleryWallpaperItem>(STATE_SELECTION)

    private var mUpdatePosition = -1

    private val mWallpapers = ArrayList<GalleryWallpaperItem>()

    private var mLastTouchPosition: Int = 0
    private var mLastTouchX: Int = 0
    private var mLastTouchY: Int = 0

    private val mGetContentActivities = ArrayList<ActivityInfo>()

    private val sRotateMenuIdsByMin = SparseIntArray()
    private val sRotateMinsByMenuId = SparseIntArray()

    init {
        sRotateMenuIdsByMin.put(0, R.id.action_rotate_interval_none)
        sRotateMenuIdsByMin.put(60, R.id.action_rotate_interval_1h)
        sRotateMenuIdsByMin.put(60 * 3, R.id.action_rotate_interval_3h)
        sRotateMenuIdsByMin.put(60 * 6, R.id.action_rotate_interval_6h)
        sRotateMenuIdsByMin.put(60 * 24, R.id.action_rotate_interval_24h)
        sRotateMenuIdsByMin.put(60 * 72, R.id.action_rotate_interval_72h)
        for (i in 0..(sRotateMenuIdsByMin.size() - 1)) {
            sRotateMinsByMenuId.put(sRotateMenuIdsByMin.valueAt(i), sRotateMenuIdsByMin.keyAt(i))
        }
    }

    private var mOptionsMenu: Menu? = null
    private var mUpdateInterval: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StyleApplication.instance.applicationComponent.inject(this)
        setContentView(R.layout.activity_gallery_setting)

        setSupportActionBar(appBar)

        mPlaceholderDrawable = ColorDrawable(ContextCompat.getColor(this,
                R.color.gallery_chosen_photo_placeholder))
        mPlaceholderSmallDrawable = ColorDrawable(ContextCompat.getColor(this,
                R.color.gallery_chosen_photo_placeholder))

        initViews()

        presenter.setView(this)
        presenter.initialize()

        maybeAttachAd(this)
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
        onDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    private fun initViews() {
        val itemAnimator = DefaultItemAnimator()
        itemAnimator.supportsChangeAnimations = false
        photoGrid.itemAnimator = itemAnimator

        setupMultiSelect()

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
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_PERMISSION)
        }

        btnGalleryEditPermissionSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        addFab.setOnClickListener {
            Analytics.logEvent(this, Event.ADD_PHOTO_CLICK)
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

    override fun renderGalleryWallpapers(wallpaperItems: List<GalleryWallpaperItem>) {
        mWallpapers.clear()
        mWallpapers.addAll(wallpaperItems)
        mChosenPhotosAdapter.notifyDataSetChanged()
        onDataSetChanged()
    }

    override fun renderUpdateInterval(intervalMin: Int) {
        mUpdateInterval = intervalMin
        val menuId = sRotateMenuIdsByMin[intervalMin]
        if (menuId != 0 && mOptionsMenu != null) {
            val item = mOptionsMenu!!.findItem(menuId)
            item?.isChecked = true
        }
    }

    override fun showLoading() {
    }

    override fun hideLoading() {
    }

    override fun showRetry() {
    }

    override fun hideRetry() {
    }

    override fun showError(message: String) {
        toast(message)
    }

    override fun context(): Context {
        return this.applicationContext
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.gallery_activity, menu)
        mOptionsMenu = menu

        val menuId = sRotateMenuIdsByMin[mUpdateInterval]
        if (menuId != 0 && mOptionsMenu != null) {
            val item = mOptionsMenu!!.findItem(menuId)
            item?.isChecked = true
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        // Make sure the 'Import photos' MenuItem is set up properly based on the number of
        // activities that handle ACTION_GET_CONTENT
        // 0 = hide the MenuItem
        // 1 = show 'Import photos from APP_NAME' to go to the one app that exists
        // 2 = show 'Import photos...' to have the user pick which app to import photos from
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        val getContentActivities = packageManager.queryIntentActivities(intent, 0)
        mGetContentActivities.clear()
        for (info in getContentActivities) {
            // Filter out the default system UI
            if (TextUtils.equals(info.activityInfo.packageName, "com.android.documentsui")) {
                continue
            }
            // Filter out non-exported activities
            if (!info.activityInfo.exported) {
                continue
            }
            // Filter out activities we don't have permission to start
            if (!TextUtils.isEmpty(info.activityInfo.permission)
                    && packageManager.checkPermission(info.activityInfo.permission,
                    packageName) != PackageManager.PERMISSION_GRANTED) {
                continue
            }
            mGetContentActivities.add(info.activityInfo)
        }

        // Hide the 'Import photos' action if there are no activities found
        val importPhotosMenuItem = menu.findItem(R.id.action_import_photos)
        importPhotosMenuItem.isVisible = !mGetContentActivities.isEmpty()
        // If there's only one app that supports ACTION_GET_CONTENT, tell the user what that app is
        if (mGetContentActivities.size == 1) {
            importPhotosMenuItem.title = getString(R.string.gallery_action_import_photos_from,
                    mGetContentActivities[0].loadLabel(packageManager))
        } else {
            importPhotosMenuItem.setTitle(R.string.gallery_action_import_photos)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        val rotateMin = sRotateMinsByMenuId.get(itemId, -1)
        if (rotateMin != -1) {
            Analytics.logEvent(this, Event.SETUP_UPDATE_INTERVAL, rotateMin.toString())
            presenter.setUpdateInterval(rotateMin)
            item.isChecked = true
            return true
        }

        if (itemId == R.id.action_import_photos) {
            Analytics.logEvent(this, Event.IMPORT_FROM_GALLERY)
            if (mGetContentActivities.size == 1) {
                // Just start the one ACTION_GET_CONTENT app
                requestGetContent(mGetContentActivities[0])
            } else {
                // Let the user pick which app they want to import photos from
                val packageManager = packageManager
                val items = arrayOfNulls<CharSequence>(mGetContentActivities.size)
                for (h in mGetContentActivities.indices) {
                    items[h] = mGetContentActivities[h].loadLabel(packageManager)
                }
                AlertDialog.Builder(this)
                        .setTitle(R.string.gallery_import_dialog_title)
                        .setItems(items, { _, which ->
                            requestGetContent(mGetContentActivities[which])
                        })
                        .show()
            }
            return true
        } else if (itemId == R.id.action_clear_photos) {
            Analytics.logEvent(this, Event.CLEAR_WALLPAPER)
            presenter.removeGalleryWallpaper(mWallpapers)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun requestGetContent(info: ActivityInfo) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setClassName(info.packageName, info.name)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, REQUEST_CHOOSE_PHOTOS)
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
        val uris = HashSet<Uri>()
        if (result.data != null) {
            uris.add(result.data)
        }
        // When selecting multiple images, "Photos" returns the first URI in getData and all URIs
        // in getClipData.
        val clipData = result.clipData
        if (clipData != null) {
            val count = clipData.itemCount
            for (i in 0..count - 1) {
                val uri = clipData.getItemAt(i).uri
                if (uri != null) {
                    uris.add(uri)
                }
            }
        }

        if (uris.isEmpty()) {
            // Nothing to do, so we can avoid posting the runnable at all
            return
        }

        presenter.addGalleryWallpaper(uris)
    }

    private fun onDataSetChanged() {
        if (mWallpapers.size > 0 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            photoGrid.visibility = View.VISIBLE
            addFab.visibility = View.VISIBLE
            empty.visibility = View.GONE
        } else {
            // No chosen images, show the empty View
            photoGrid.visibility = View.GONE
            addFab.visibility = View.GONE
            empty.visibility = View.VISIBLE
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, we can show the random camera photos image
                emptyAnimator.displayedChild = 0
                emptyDescription.setText(R.string.gallery_empty)
                addFab.visibility = View.VISIBLE
            } else {
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

    override fun onBackPressed() {
        if (mMultiSelectionController.getSelectedCount() > 0) {
            mMultiSelectionController.reset(true)
        } else if (addToolbar.visibility == View.VISIBLE) {
            hideAddToolbar(true)
        } else {
            super.onBackPressed()
        }
    }

    private fun tryUpdateSelection(allowAnimate: Boolean) {
        if (mUpdatePosition >= 0) {
            mChosenPhotosAdapter.notifyItemChanged(mUpdatePosition)
            mUpdatePosition = -1
        } else {
            mChosenPhotosAdapter.notifyDataSetChanged()
        }

        val selectedCount = mMultiSelectionController.getSelectedCount()
        val toolbarVisible = selectedCount > 0
        var showForceNow = selectedCount == 1
        if (showForceNow) {
            // Double check to make sure we can force a URI for the selected URI
            val selectedItem = mMultiSelectionController.getSelection().iterator().next()
            // Only show the force now icon if it isn't a tree URI or there is at least one image in the tree
            showForceNow = !selectedItem.isTreeUri
                    || !getImagesFromTreeUri(this, Uri.parse(selectedItem.uri), 1).isEmpty()
        }
        selectionToolbar.menu.findItem(R.id.action_force_now).isVisible = showForceNow

        val tag = selectionToolbarContainer.getTag(0xDEADBEEF.toInt())
        val previouslyVisible = if (tag == null) false else tag as Boolean

        if (previouslyVisible != toolbarVisible) {
            selectionToolbarContainer.setTag(0xDEADBEEF.toInt(), toolbarVisible)

            val duration = if (allowAnimate)
                resources.getInteger(android.R.integer.config_shortAnimTime)
            else
                0
            if (toolbarVisible) {
                selectionToolbarContainer.visibility = View.VISIBLE
                selectionToolbarContainer.translationY =
                        (-selectionToolbarContainer.height).toFloat()
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
                val selectedItem = mMultiSelectionController.getSelection().iterator().next()
                if (selectedItem.isTreeUri) {
                    val displayName = getDisplayNameForTreeUri(this, Uri.parse(selectedItem.uri))
                    if (!TextUtils.isEmpty(displayName)) {
                        title = displayName
                    }
                }
            }
            selectionToolbar.title = title
        }
    }

    private fun setupMultiSelect() {
        // Set up toolbar
        selectionToolbar.setNavigationOnClickListener {
            mMultiSelectionController.reset(true)
        }

        selectionToolbar.inflateMenu(R.menu.gallery_selection)
        selectionToolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            val itemId = item.itemId
            if (itemId == R.id.action_force_now) {
                val selection = mMultiSelectionController.getSelection()
                if (selection.isNotEmpty()) {
                    val selectedItems = selection.iterator().next()
                    presenter.forceNow(selectedItems.uri)
                    toast(R.string.gallery_temporary_force_image)
                }
                mMultiSelectionController.reset(true)
                return@OnMenuItemClickListener true
            } else if (itemId == R.id.action_remove) {
                val removeItems = ArrayList<GalleryWallpaperItem>(
                        mMultiSelectionController.getSelection())
                presenter.removeGalleryWallpaper(removeItems)
                mMultiSelectionController.reset(true)
                return@OnMenuItemClickListener true
            }
            false
        })

        // Set up controller
        mMultiSelectionController.setCallbacks(object : MultiSelectionController.Callbacks {
            override fun onSelectionChanged(restored: Boolean, fromUser: Boolean) {
                tryUpdateSelection(!restored)
            }
        })
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
            val wallpaperItem = mWallpapers[position]
            return if (wallpaperItem.isTreeUri)
                ITEM_TYPE_TREE
            else ITEM_TYPE_URI
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckableViewHolder {
            val isTreeUri = viewType != 0
            val v: View
            val vh: CheckableViewHolder
            if (isTreeUri) {
                v = LayoutInflater.from(this@GallerySettingActivity)
                        .inflate(R.layout.gallery_chosen_photo_tree_item, parent, false)
                vh = TreeViewHolder(v)
            } else {
                v = LayoutInflater.from(this@GallerySettingActivity)
                        .inflate(R.layout.gallery_chosen_photo_item, parent, false)
                vh = PhotoViewHolder(v)
            }

            v.layoutParams.height = mItemSize
            v.setOnTouchListener { _, motionEvent ->
                if (motionEvent.actionMasked != MotionEvent.ACTION_CANCEL) {
                    mLastTouchPosition = vh.adapterPosition
                    mLastTouchX = motionEvent.x.toInt()
                    mLastTouchY = motionEvent.y.toInt()
                }
                false
            }
            v.setOnClickListener {
                mUpdatePosition = vh.adapterPosition
                if (mUpdatePosition != RecyclerView.NO_POSITION) {
                    mMultiSelectionController.toggle(mWallpapers[mUpdatePosition], true)
                }
            }
            return vh
        }

        override fun onBindViewHolder(vh: CheckableViewHolder, position: Int) {
            val wallpaperItem = mWallpapers[position]
            val isTreeUri = getItemViewType(position) != 0
            if (isTreeUri) {
                val treeVh = vh as TreeViewHolder
                val maxImages = treeVh.mThumbViews.size
                val imageUri = Uri.parse(wallpaperItem.uri)
                val images = getImagesFromTreeUri(this@GallerySettingActivity, imageUri, maxImages)
                val numImages = images.size
                for (h in 0..numImages - 1) {
                    Glide.with(this@GallerySettingActivity)
                            .load(images[h])
                            .override(mItemSize / 2, mItemSize / 2)
                            .placeholder(mPlaceholderSmallDrawable)
                            .into(treeVh.mThumbViews[h])
                }
                for (h in numImages..maxImages - 1) {
                    treeVh.mThumbViews[h].setImageDrawable(mPlaceholderSmallDrawable)
                }
            } else {
                val photoVh = vh as PhotoViewHolder
                Glide.with(this@GallerySettingActivity)
                        .load(Uri.parse(wallpaperItem.uri))
                        .override(mItemSize, mItemSize)
                        .placeholder(mPlaceholderDrawable)
                        .into(photoVh.mThumbView)
            }

            val checked = mMultiSelectionController.isSelected(wallpaperItem)
            vh.mRootView.setTag(R.id.gallery_viewtag_position, position)
            if (mLastTouchPosition == vh.adapterPosition
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Handler().post {
                    if (!vh.mCheckedOverlayView.isAttachedToWindow) {
                        // Can't animate detached Views
                        vh.mCheckedOverlayView.visibility = if (checked) View.VISIBLE else View.GONE
                        return@post
                    }
                    if (checked) {
                        vh.mCheckedOverlayView.visibility = View.VISIBLE
                    }

                    // find the smallest radius that'll cover the item
                    val coverRadius = maxDistanceToCorner(
                            mLastTouchX, mLastTouchY,
                            0, 0, vh.mRootView.width, vh.mRootView.height)

                    val revealAnim = ViewAnimationUtils.createCircularReveal(
                            vh.mCheckedOverlayView,
                            mLastTouchX,
                            mLastTouchY,
                            if (checked) 0f else coverRadius,
                            if (checked) coverRadius else 0f)
                            .setDuration(150)

                    if (!checked) {
                        revealAnim.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                vh.mCheckedOverlayView.visibility = View.GONE
                            }
                        })
                    }
                    revealAnim.start()
                }
            } else {
                vh.mCheckedOverlayView.visibility = if (checked) View.VISIBLE else View.GONE
            }
        }

        private fun maxDistanceToCorner(x: Int, y: Int, left: Int, top: Int,
                                        right: Int, bottom: Int): Float {
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
            return mWallpapers.size
        }

        override fun getItemId(position: Int): Long {
            return mWallpapers[position].id
        }
    }
}