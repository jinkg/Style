package com.yalin.style.data.repository.datasource.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.yalin.style.data.R;
import com.yalin.style.data.log.LogUtil;
import com.yalin.style.data.repository.datasource.provider.StyleContract.AdvanceWallpaper;
import com.yalin.style.data.repository.datasource.provider.StyleContract.Wallpaper;
import com.yalin.style.data.repository.datasource.sync.account.Account;

/**
 * YaLin 2016/12/30.
 */

public class StyleDatabase extends SQLiteOpenHelper {

    private static final String TAG = "StyleDatabase";
    private static final String DATABASE_NAME = "style.db";

    private static final int VERSION_2016_12_30 = 1;
    private static final int VERSION_2017_4_30 = 2;
    private static final int VERSION_2017_5_24 = 3;
    private static final int VERSION_2017_7_28 = 4;
    private static final int VERSION_2017_8_11 = 5;
    private static final int CUR_DATABASE_VERSION = VERSION_2017_8_11;

    private final Context mContext;

    interface Tables {

        String WALLPAPER = StyleContract.Wallpaper.TABLE_NAME;
        String GALLERY = StyleContract.GalleryWallpaper.TABLE_NAME;
        String ADVANCE_WALLPAPER = AdvanceWallpaper.TABLE_NAME;
    }

    public StyleDatabase(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.WALLPAPER + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + StyleContract.Wallpaper.COLUMN_NAME_WALLPAPER_ID + " TEXT,"
                + StyleContract.Wallpaper.COLUMN_NAME_TITLE + " TEXT,"
                + StyleContract.Wallpaper.COLUMN_NAME_IMAGE_URI + " TEXT,"
                + StyleContract.Wallpaper.COLUMN_NAME_ATTRIBUTION + " TEXT,"
                + StyleContract.Wallpaper.COLUMN_NAME_BYLINE + " TEXT,"
                + StyleContract.Wallpaper.COLUMN_NAME_ADD_DATE + " INTEGER);");

        upgradeFrom20161230to20170430(db);
        upgradeFrom20170430to20170524(db);
        upgradeFrom20170525to20170728(db);
        upgradeFrom20170728to20170811(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.D(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
        // Cancel any sync currently in progress
        android.accounts.Account account = Account.getAccount();
        if (account != null) {
            LogUtil.D(TAG, "Cancelling any pending syncs for account");
            ContentResolver.cancelSync(account, mContext.getString(R.string.authority));
        }
        int version = oldVersion;
        if (version == VERSION_2016_12_30) {
            upgradeFrom20161230to20170430(db);
            version = VERSION_2017_4_30;
        }
        if (version == VERSION_2017_4_30) {
            upgradeFrom20170430to20170524(db);
            version = VERSION_2017_5_24;
        }
        if (version == VERSION_2017_5_24) {
            upgradeFrom20170525to20170728(db);
            version = VERSION_2017_7_28;
        }
        if (version == VERSION_2017_7_28) {
            upgradeFrom20170728to20170811(db);
            version = VERSION_2017_8_11;
        }
        if (version != CUR_DATABASE_VERSION) {
            LogUtil.E(TAG, "Upgrade unsuccessful -- destroying old data during upgrade");

            db.execSQL("DROP TABLE IF EXISTS " + Tables.WALLPAPER);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.GALLERY);
            onCreate(db);
            version = CUR_DATABASE_VERSION;
        }
    }

    private void upgradeFrom20161230to20170430(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + Tables.WALLPAPER
                + " ADD COLUMN " + Wallpaper.COLUMN_NAME_LIKED + " INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE " + Tables.WALLPAPER
                + " ADD COLUMN " + Wallpaper.COLUMN_NAME_CHECKSUM + " TEXT");
    }

    private void upgradeFrom20170430to20170524(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.GALLERY + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + StyleContract.GalleryWallpaper.COLUMN_NAME_CUSTOM_URI + " TEXT NOT NULL,"
                + StyleContract.GalleryWallpaper.COLUMN_NAME_IS_TREE_URI + " INTEGER,"
                + StyleContract.GalleryWallpaper.COLUMN_NAME_DATE_TIME + " INTEGER,"
                + StyleContract.GalleryWallpaper.COLUMN_NAME_LOCATION + " TEXT,"
                + StyleContract.GalleryWallpaper.COLUMN_NAME_HAS_METADATA + " INTEGER DEFAULT 0,"
                + "UNIQUE (" + StyleContract.GalleryWallpaper.COLUMN_NAME_CUSTOM_URI
                + ") ON CONFLICT REPLACE);");
    }

    private void upgradeFrom20170525to20170728(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.ADVANCE_WALLPAPER + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + AdvanceWallpaper.COLUMN_NAME_WALLPAPER_ID + " TEXT,"
                + AdvanceWallpaper.COLUMN_NAME_ICON_URL + " TEXT,"
                + AdvanceWallpaper.COLUMN_NAME_DOWNLOAD_URL + " TEXT,"
                + AdvanceWallpaper.COLUMN_NAME_NAME + " TEXT,"
                + AdvanceWallpaper.COLUMN_NAME_AUTHOR + " TEXT,"
                + AdvanceWallpaper.COLUMN_NAME_STORE_PATH + " TEXT,"
                + AdvanceWallpaper.COLUMN_NAME_LINK + " TEXT,"
                + AdvanceWallpaper.COLUMN_NAME_PROVIDER_NAME + " TEXT,"
                + AdvanceWallpaper.COLUMN_NAME_CHECKSUM + " TEXT,"
                + AdvanceWallpaper.COLUMN_NAME_SELECTED + " INTEGER DEFAULT 0);");
    }

    private void upgradeFrom20170728to20170811(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + Tables.ADVANCE_WALLPAPER
                + " ADD COLUMN " + AdvanceWallpaper.COLUMN_NAME_LAZY_DOWNLOAD
                + " INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE " + Tables.ADVANCE_WALLPAPER
                + " ADD COLUMN " + AdvanceWallpaper.COLUMN_NAME_NEED_AD
                + " INTEGER NOT NULL DEFAULT 0");
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
