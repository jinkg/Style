package com.yalin.style.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.yalin.style.provider.StyleContract.Wallpaper;
import com.yalin.style.provider.StyleDatabase.Tables;
import com.yalin.style.util.LogUtil;
import com.yalin.style.util.SelectionBuilder;
import com.yalin.style.util.WallpaperFileHelper;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Random;

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

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, String[] projection, String selection,
      String[] selectionArgs,
      String sortOrder) {
    final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

    StyleUriEnum uriEnum = mUriMatcher.matchUri(uri);

    LogUtil.d(TAG, "uri=" + uri + " code=" + uriEnum.code + " proj=" +
        Arrays.toString(projection) + " selection=" + selection + " args="
        + Arrays.toString(selectionArgs) + ")");

    switch (uriEnum) {
      case WALLPAPER:
      case WALLPAPER_ID: {
        final SelectionBuilder builder = buildSimpleSelection(uri);
        return builder.query(db, projection, null);
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
    LogUtil.d(TAG, "insert(uri=" + uri + ", values=" + values.toString()
        + ")");

    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    StyleUriEnum uriEnum = mUriMatcher.matchUri(uri);
    db.insertOrThrow(uriEnum.table, null, values);

    switch (uriEnum) {
      case WALLPAPER:
        return Wallpaper.buildWallpaperUri(values.getAsString(Wallpaper.COLUMN_NAME_WALLPAPER_ID));
      default: {
        throw new UnsupportedOperationException("Unknown insert uri: " + uri);
      }
    }
  }

  @Override
  public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
    LogUtil.d(TAG, "delete(uri=" + uri + ")");
    if (uri == StyleContract.BASE_CONTENT_URI) {
      deleteDatabase();
      return 1;
    }
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final SelectionBuilder builder = buildSimpleSelection(uri);
    int retVal = builder.where(selection, selectionArgs).delete(db);
    notifyChange(uri);
    return retVal;
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {
    return 0;
  }

  @Nullable
  @Override
  public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
      throws FileNotFoundException {
    LogUtil.d(TAG, "openReadFile(uri=" + uri + ",mode=" + mode + ")");

    StyleUriEnum uriEnum = mUriMatcher.matchUri(uri);
    switch (uriEnum) {
      case WALLPAPER:
        return WallpaperFileHelper.openReadFile(getContext(), queryUriForShow(), mode);
      case WALLPAPER_ID:
        return WallpaperFileHelper.openWriteFile(getContext(), uri, mode);
      default:
        return null;
    }
  }

  private Uri queryUriForShow() {
    Cursor cursor = query(Wallpaper.CONTENT_URI, new String[]{Wallpaper.COLUMN_NAME_IMAGE_URI},
        null, null, null);
    try {
      if (cursor != null && cursor.moveToFirst()) {
        Random random = new Random();
        int position = random.nextInt(cursor.getCount());
        cursor.moveToPosition(position);

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
      case WALLPAPER: {
        return builder.table(uriEnum.table);
      }
      case WALLPAPER_ID: {
        String wallpaperId = Wallpaper.getWallpaperId(uri);
        return builder.table(Tables.WALLPAPER)
            .where(Wallpaper.COLUMN_NAME_WALLPAPER_ID + " = ?", wallpaperId);
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
      case LAST_WALLPAPER: {
        return builder.table(Tables.WALLPAPER).where(Wallpaper.COLUMN_NAME_ADD_DATE +
            " = (SELECT max(" + Wallpaper.COLUMN_NAME_ADD_DATE + ") FROM " + Tables.WALLPAPER
            + ")");
      }
      default: {
        throw new UnsupportedOperationException("Unknown uri: " + uri);
      }
    }
  }

  private void notifyChange(Uri uri) {

  }
}
