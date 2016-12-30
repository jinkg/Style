package com.yalin.style.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.yalin.style.provider.StyleContract.Wallpaper;
import com.yalin.style.provider.StyleDatabase.Tables;
import com.yalin.style.util.LogUtil;
import com.yalin.style.util.SelectionBuilder;
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
        return null;
    }

    return null;
  }

  @Override
  public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {
    return 0;
  }

  private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
    final SelectionBuilder builder = new SelectionBuilder();
    StyleUriEnum uriEnum = mUriMatcher.matchUri(uri);
    if (uriEnum == null) {
      throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
    switch (uriEnum) {
      case LAST_WALLPAPER: {
        return builder.table(Tables.WALLPAPER).where(Wallpaper.COLUMN_NAME_ADD_DATE + " = ?",
            "SELECT max(" + Wallpaper.COLUMN_NAME_ADD_DATE + ")");
      }
      default: {
        throw new UnsupportedOperationException("Unknown uri: " + uri);
      }
    }
  }
}
