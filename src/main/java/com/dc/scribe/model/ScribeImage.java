package com.dc.scribe.model;

import android.net.Uri;

/**
 * Created by david on 13/08/16.
 */
public class ScribeImage {

    private Uri imgResourceUrl;

    public ScribeImage(Uri imageResourceUrl) {
        this.imgResourceUrl = imageResourceUrl;
    }

    public Uri getImgResourceUrl() {
        return imgResourceUrl;
    }
}
