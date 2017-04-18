package com.yalin.style.data.repository.datasource.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * YaLin 2016/12/30.
 */

public class StyleDatabase extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "style.db";

  private static final int VERSION_2016_12_30 = 1;

  private final Context mContext;

  interface Tables {

    String WALLPAPER = StyleContract.Wallpaper.TABLE_NAME;
  }

  public StyleDatabase(Context context) {
    super(context, DATABASE_NAME, null, VERSION_2016_12_30);
    mContext = context;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + StyleContract.Wallpaper.TABLE_NAME + " ("
        + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + StyleContract.Wallpaper.COLUMN_NAME_WALLPAPER_ID + " TEXT,"
        + StyleContract.Wallpaper.COLUMN_NAME_TITLE + " TEXT,"
        + StyleContract.Wallpaper.COLUMN_NAME_IMAGE_URI + " TEXT,"
        + StyleContract.Wallpaper.COLUMN_NAME_ATTRIBUTION + " TEXT,"
        + StyleContract.Wallpaper.COLUMN_NAME_BYLINE + " TEXT,"
        + StyleContract.Wallpaper.COLUMN_NAME_ADD_DATE + " INTEGER);");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }

  public static void deleteDatabase(Context context) {
    context.deleteDatabase(DATABASE_NAME);
  }
}
