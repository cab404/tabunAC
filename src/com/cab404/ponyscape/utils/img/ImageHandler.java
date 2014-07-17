package com.cab404.ponyscape.utils.img;

import android.graphics.Bitmap;

/**
 * Handles a delivered bitmap or an error.
 *
 * @author cab404
 */
public interface ImageHandler {

    public void onSuccess(Bitmap received);
    public void onFailure(Exception e);

}
