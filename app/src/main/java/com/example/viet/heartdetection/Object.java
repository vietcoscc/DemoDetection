package com.example.viet.heartdetection;

import android.graphics.Bitmap;

/**
 * Created by viet on 16/01/2018.
 */

public class Object {
    private Bitmap bitmap;
    private String info;

    public Object(Bitmap bitmap, String info) {
        this.bitmap = bitmap;
        this.info = info;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
