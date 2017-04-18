package com.yalin.style.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.yalin.style.data.repository.datasource.provider.StyleContract.Wallpaper;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.Assert;
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
    contentValues.put(Wallpaper.COLUMN_NAME_WALLPAPER_ID, "id");
    contentValues.put(Wallpaper.COLUMN_NAME_TITLE, "title111");
    contentValues.put(Wallpaper.COLUMN_NAME_ATTRIBUTION, "yalin...");
    contentValues.put(Wallpaper.COLUMN_NAME_BYLINE, "byline111");
    contentValues.put(Wallpaper.COLUMN_NAME_IMAGE_URI, "xxx");
    contentValues.put(Wallpaper.COLUMN_NAME_ADD_DATE, System.currentTimeMillis());
    Uri uri = context.getContentResolver().insert(Wallpaper.CONTENT_URI, contentValues);

    try {
      Cursor cursor = context.getContentResolver()
          .query(Wallpaper.CONTENT_LAST_WALLPAPER_URI, null, null, null, null);

      Assert.assertNotNull(uri);
      Assert.assertNotNull(cursor);
      Assert.assertTrue(cursor.getCount() == 1);

      if (cursor.moveToFirst()) {
        String title = cursor.getString(cursor.getColumnIndex(Wallpaper.COLUMN_NAME_TITLE));

        Assert.assertEquals(title, "title111");
      }
      cursor.close();
    } finally {
      if (uri != null) {
        int retVal = context.getContentResolver().delete(uri, null, null);
        Assert.assertEquals(retVal, 1);
      }
    }
  }

  @Test
  public void testOpenFile() {
    Context context = InstrumentationRegistry.getTargetContext();
    try {
      InputStream is = context.getContentResolver().openInputStream(Wallpaper.CONTENT_URI);
      Assert.assertNotNull(is);
      is.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
