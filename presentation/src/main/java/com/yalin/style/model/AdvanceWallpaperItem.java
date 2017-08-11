package com.yalin.style.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author jinyalin
 * @since 2017/7/28.
 */

public class AdvanceWallpaperItem implements Parcelable {
    public long id;
    public String wallpaperId;
    public String link;
    public String name;
    public String author;
    public String iconUrl;
    public String downloadUrl;

    public boolean lazyDownload;
    public boolean needAd;

    public String providerName;

    public String storePath;

    public boolean isSelected;

    public AdvanceWallpaperItem() {
    }

    protected AdvanceWallpaperItem(Parcel in) {
        id = in.readLong();
        wallpaperId = in.readString();
        link = in.readString();
        name = in.readString();
        author = in.readString();
        iconUrl = in.readString();
        downloadUrl = in.readString();
        lazyDownload = in.readByte() != 0;
        needAd = in.readByte() != 0;
        providerName = in.readString();
        storePath = in.readString();
        isSelected = in.readByte() != 0;
    }

    public static final Creator<AdvanceWallpaperItem> CREATOR = new Creator<AdvanceWallpaperItem>() {
        @Override
        public AdvanceWallpaperItem createFromParcel(Parcel in) {
            return new AdvanceWallpaperItem(in);
        }

        @Override
        public AdvanceWallpaperItem[] newArray(int size) {
            return new AdvanceWallpaperItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(wallpaperId);
        dest.writeString(link);
        dest.writeString(name);
        dest.writeString(author);
        dest.writeString(iconUrl);
        dest.writeString(downloadUrl);
        dest.writeByte((byte) (lazyDownload ? 1 : 0));
        dest.writeByte((byte) (needAd ? 1 : 0));
        dest.writeString(providerName);
        dest.writeString(storePath);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }
}
