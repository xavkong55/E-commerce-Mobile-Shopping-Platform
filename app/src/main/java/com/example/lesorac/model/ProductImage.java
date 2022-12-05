package com.example.lesorac.model;

import android.net.Uri;

public class ProductImage {
    private Uri uri;
    private String URL;

    public ProductImage(String URL) {
        this.URL = URL;
    }

    public ProductImage(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}
