package com.yalin.style.data.repository.datasource.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yalin.style.data.log.LogUtil;
import com.yalin.style.data.repository.datasource.provider.StyleContract.AdvanceWallpaper;
import com.yalin.style.data.repository.datasource.provider.StyleContract.GalleryWallpaper;
import com.yalin.style.data.repository.datasource.provider.StyleContract.Wallpaper;
import com.yalin.style.data.utils.SelectionBuilder;
import com.yalin.style.data.utils.WallpaperFileHelper;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * YaLin 2016/12/30.
 */

public class StyleProvider extends ContentProvider {

    private static final String TAG = "StyleProvider";

    private StyleDatabase mOpenHelper;

    private StyleProviderUriMatcher mUriMatcher;

    @Override
    public boolean onCreate() {
        mOpenHelper = new StyleDatabase(getContext());
        mUriMatcher = new StyleProviderUriMatcher();
        return true;
    }

    private void deleteDatabase() {
        mOpenHelper.close();
        Context context = getContext();
        StyleDatabase.deleteDatabase(context);
        mOpenHelper = new StyleDatabase(context);
    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        StyleUriEnum uriEnum = mUriMatcher.matchUri(uri);

        LogUtil.D(TAG, "uri=" + uri + " code=" + uriEnum.code + " proj=" +
                Arrays.toString(projection) + " selection=" + selection + " args="
                + Arrays.toString(selectionArgs) + ")");

        switch (uriEnum) {
            case WALLPAPER:
            case WALLPAPER_ID:
            case WALLPAPER_LIKED:
            case ADVANCE_WALLPAPER:
            case ADVANCE_WALLPAPER_ID:
            case ADVANCE_WALLPAPER_SELECTED: {
                final SelectionBuilder builder = buildSimpleSelection(uri);
                return builder.query(db, projection, Wallpaper._ID + " DESC");
            }
            case GALLERY:
            case GALLERY_ID: {
                final SelectionBuilder builder = buildSimpleSelection(uri);
                return builder.query(db, projection, GalleryWallpaper._ID + " DESC");
            }
            default: {
                final SelectionBuilder builder = buildExpandedSelection(uri, uriEnum.code);

                return builder.query(db, projection, null);
            }
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        LogUtil.D(TAG, "insert(uri=" + uri + ", values=" + values.toString()
                + ")");

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        StyleUriEnum uriEnum = mUriMatcher.matchUri(uri);
        long id = db.insertOrThrow(uriEnum.table, null, values);

        switch (uriEnum) {
            case WALLPAPER:
                return StyleContract.Wallpaper.buildWallpaperUri(
                        values.getAsString(StyleContract.Wallpaper.COLUMN_NAME_WALLPAPER_ID));
            case GALLERY:
                return StyleContract.GalleryWallpaper.buildGalleryWallpaperUri(id);
            case ADVANCE_WALLPAPER: {
                return AdvanceWallpaper.buildWallpaperUri(
                        values.getAsString(AdvanceWallpaper.COLUMN_NAME_WALLPAPER_ID));
            }
            default: {
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
            }
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        LogUtil.D(TAG, "delete(uri=" + uri + ")");
        if (uri == StyleContract.BASE_CONTENT_URI) {
            deleteDatabase();
            return 1;
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        StyleUriEnum uriEnum = mUriMatcher.matchUri(uri);
        final SelectionBuilder builder = buildSimpleSelection(uri);
        switch (uriEnum) {
            case WALLPAPER: {
                builder.where(Wallpaper.COLUMN_NAME_LIKED + " = ?", "0");
                break;
            }
            case ADVANCE_WALLPAPER_SELECTED: {
                builder.where(AdvanceWallpaper.COLUMN_NAME_SELECTED + " = ?", "0");
                break;
            }
            case GALLERY_URI: {
                return builder.delete(db);
            }
        }
        return builder.where(selection, selectionArgs).delete(db);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        LogUtil.D(TAG, "update(uri=" + uri + ")");
        if (uri == StyleContract.BASE_CONTENT_URI) {
            deleteDatabase();
            return 1;
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        return builder.where(selection, selectionArgs).update(db, values);
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
            throws FileNotFoundException {
        LogUtil.D(TAG, "openReadFile(uri=" + uri + ",mode=" + mode + ")");

        StyleUriEnum uriEnum = mUriMatcher.matchUri(uri);
        switch (uriEnum) {
            case WALLPAPER:
                return WallpaperFileHelper.openReadFile(getContext(), queryUriForShow(), mode);
            case WALLPAPER_ID:
                return WallpaperFileHelper.openReadFile(getContext(), uri, mode);
            case WALLPAPER_SAVE:
                return WallpaperFileHelper.openWriteFile(getContext(), uri, mode);
            default:
                throw new FileNotFoundException("Cannot match uri : " + uri);
        }
    }

    private Uri queryUriForShow() {
        Cursor cursor = query(Wallpaper.CONTENT_URI,
                new String[]{StyleContract.Wallpaper.COLUMN_NAME_IMAGE_URI},
                null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return Uri.parse(cursor.getString(0));
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        StyleUriEnum uriEnum = mUriMatcher.matchUri(uri);

        switch (uriEnum) {
            case WALLPAPER:
            case ADVANCE_WALLPAPER: {
                return builder.table(uriEnum.table);
            }
            case WALLPAPER_ID: {
                String wallpaperId = StyleContract.Wallpaper.getWallpaperId(uri);
                return builder.table(StyleDatabase.Tables.WALLPAPER)
                        .where(Wallpaper.COLUMN_NAME_WALLPAPER_ID + " = ?", wallpaperId);
            }
            case WALLPAPER_LIKE: {
                String wallpaperId = StyleContract.Wallpaper.getWallpaperLikeId(uri);
                return builder.table(StyleDatabase.Tables.WALLPAPER)
                        .where(Wallpaper.COLUMN_NAME_WALLPAPER_ID + " = ?", wallpaperId);
            }
            case WALLPAPER_LIKED: {
                return builder.table(StyleDatabase.Tables.WALLPAPER)
                        .where(Wallpaper.COLUMN_NAME_LIKED + " = ?", "1");
            }
            case GALLERY: {
                return builder.table(StyleDatabase.Tables.GALLERY);
            }
            case GALLERY_ID: {
                String galleryWallpaperId
                        = StyleContract.GalleryWallpaper.getGalleryWallpaperId(uri);
                return builder.table(StyleDatabase.Tables.GALLERY)
                        .where(GalleryWallpaper._ID + " = ?", galleryWallpaperId);
            }
            case GALLERY_URI: {
                String uriString
                        = StyleContract.GalleryWallpaper.getGalleryWallpaperDeleteUri(uri);
                return builder.table(StyleDatabase.Tables.GALLERY)
                        .where(GalleryWallpaper.COLUMN_NAME_CUSTOM_URI + " = ?", uriString);
            }
            case ADVANCE_WALLPAPER_ID: {
                String wallpaperId = AdvanceWallpaper.getWallpaperId(uri);
                return builder.table(StyleDatabase.Tables.ADVANCE_WALLPAPER)
                        .where(AdvanceWallpaper.COLUMN_NAME_WALLPAPER_ID + " = ?", wallpaperId);
            }
            case ADVANCE_WALLPAPER_SELECTED: {
                return builder.table(StyleDatabase.Tables.ADVANCE_WALLPAPER)
                        .where(AdvanceWallpaper.COLUMN_NAME_SELECTED + " = ?", String.valueOf(1));
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri for " + uri);
            }
        }
    }

    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        StyleUriEnum uriEnum = mUriMatcher.matchUri(uri);
        if (uriEnum == null) {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        switch (uriEnum) {
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }
}
