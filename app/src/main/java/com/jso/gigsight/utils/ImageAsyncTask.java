package com.jso.gigsight.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

public class ImageAsyncTask extends AsyncTask<URL, Void, Bitmap> {

    private final WeakReference<ImageView> containerImageView;

    public ImageAsyncTask(ImageView imageView) {
        this.containerImageView = new WeakReference<ImageView>(imageView);
    }

    @Override
    protected Bitmap doInBackground(URL... params) {
        URL imageURL = params[0];
        Bitmap bitmap = null;

        try {
            InputStream inputStream = imageURL.openStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        ImageView imageView = this.containerImageView.get();
        if (imageView != null) {
            imageView.setImageBitmap(result);
        }
    }
}