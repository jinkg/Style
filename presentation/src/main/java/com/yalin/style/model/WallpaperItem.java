package com.yalin.style.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * YaLin 2017/1/3.
 */

public class WallpaperItem implements Parcelable {

    public String wallpaperId;
    public String imageUri;
    public String title;
    public String byline;
    public String attribution;

    public boolean liked;
    public boolean isDefault;

    public WallpaperItem() {
    }

    protected WallpaperItem(Parcel in) {
        wallpaperId = in.readString();
        imageUri = in.readString();
        title = in.readString();
        byline = in.readString();
        attribution = in.readString();
        liked = in.readByte() != 0;
        isDefault = in.readByte() != 0;
    }

    public static final Creator<WallpaperItem> CREATOR = new Creator<WallpaperItem>() {
        @Override
        public WallpaperItem createFromParcel(Parcel in) {
            return new WallpaperItem(in);
        }

        @Override
        public WallpaperItem[] newArray(int size) {
            return new WallpaperItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(wallpaperId);
        dest.writeString(imageUri);
        dest.writeString(title);
        dest.writeString(byline);
        dest.writeString(attribution);
        dest.writeByte((byte) (liked ? 1 : 0));
        dest.writeByte((byte) (isDefault ? 1 : 0));
    }
}
