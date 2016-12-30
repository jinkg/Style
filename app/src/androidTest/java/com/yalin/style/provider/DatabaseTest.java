package com.yalin.style.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.yalin.style.provider.StyleContract.Wallpaper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * YaLin 2016/12/30.
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

  @Test
  public void testInsertWallpaper() {
    Context context = InstrumentationRegistry.getTargetContext();

    ContentValues contentValues = new ContentValues();
    contentValues.put(Wallpaper.COLUMN_NAME_TITLE, "title111");
    contentValues.put(Wallpaper.COLUMN_NAME_ATTRIBUTION, "yalin...");
    contentValues.put(Wallpaper.COLUMN_NAME_BYLINE, "byline111");
    contentValues.put(Wallpaper.COLUMN_NAME_IMAGE_URI, "xxx");
    contentValues.put(Wallpaper.COLUMN_NAME_ADD_DATE, System.currentTimeMillis());
    context.getContentResolver().insert(Wallpaper.CONTENT_URI, contentValues);

    Cursor cursor = context.getContentResolver()
        .query(Wallpaper.CONTENT_LAST_WALLPAPER_URI, null, null, null, null);

    if (cursor != null && cursor.moveToFirst()) {
      String title = cursor.getString(cursor.getColumnIndex(Wallpaper.COLUMN_NAME_TITLE));
      System.out.println(title);
    }
  }
}
