package com.cab404.ponyscape.utils.img.handlers;

import android.graphics.Bitmap;
import android.widget.ImageView;
import com.cab404.ponyscape.utils.img.ImageHandler;

import java.lang.ref.WeakReference;

/**
 * @author cab404
 */
public class ImageViewHandler implements ImageHandler {
    WeakReference<ImageView> link;

    public ImageViewHandler(ImageView view) {
        link = new WeakReference<>(view);
    }

    @Override public void onSuccess(final Bitmap received) {
        final ImageView view = link.get();
        if (view != null) {
            view.post(new Runnable() {
                @Override public void run() {
                    view.setImageBitmap(received);
                }
            });
        }
    }

    @Override public void onFailure(Exception e) {

    }
}
