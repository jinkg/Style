package com.yalin.style.provider;

import android.content.UriMatcher;
import android.net.Uri;
import android.util.SparseArray;

/**
 * YaLin 2016/12/30.
 */

public class StyleProviderUriMatcher {

  private UriMatcher mUriMatcher;

  private SparseArray<StyleUriEnum> mEnumsMap = new SparseArray<>();

  public StyleProviderUriMatcher() {
    mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    buildUriMatcher();
  }

  private void buildUriMatcher() {
    final String authority = StyleContract.AUTHORITY;

    StyleUriEnum[] uris = StyleUriEnum.values();
    for (StyleUriEnum uri : uris) {
      mUriMatcher.addURI(authority, uri.path, uri.code);
    }
    buildEnumsMap();
  }

  private void buildEnumsMap() {
    StyleUriEnum[] uris = StyleUriEnum.values();
    for (StyleUriEnum uri : uris) {
      mEnumsMap.put(uri.code, uri);
    }
  }

  public StyleUriEnum matchUri(Uri uri) {
    final int code = mUriMatcher.match(uri);
    try {
      return matchCode(code);
    } catch (UnsupportedOperationException e) {
      throw new UnsupportedOperationException("Unknown uri " + uri);
    }
  }

  public StyleUriEnum matchCode(int code) {
    StyleUriEnum uriEnum = mEnumsMap.get(code);
    if (uriEnum != null) {
      return uriEnum;
    } else {
      throw new UnsupportedOperationException("Unknown uri with code " + code);
    }
  }
}
