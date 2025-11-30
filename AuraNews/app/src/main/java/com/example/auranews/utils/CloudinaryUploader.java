package com.example.auranews.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.InputStream;
import java.util.Map;

public class CloudinaryUploader {

    public interface UploadCallback {
        void onSuccess(String url);
        void onError(Exception e);
    }

    private static final String TAG = "CloudinaryUploader";
    private final Cloudinary cloudinary;

    public CloudinaryUploader() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dv9hgofvs",
                "api_key", "111887932449724",
                "api_secret", "IP437Sb4VlMiZ8FLAl0vPXQ8IRU"
        ));
    }

    public void uploadImage(Context context, Uri uri, UploadCallback callback) {
        new Thread(() -> {
            try {
                InputStream inputStream =
                        context.getContentResolver().openInputStream(uri);
                if (inputStream == null) throw new Exception("InputStream null");

                Map upload = cloudinary.uploader().upload(
                        inputStream,
                        ObjectUtils.asMap("resource_type", "image")
                );
                String url = upload.get("url").toString();
                inputStream.close();

                Log.d(TAG, "Upload success, url = " + url);

                if (callback != null) callback.onSuccess(url);
            } catch (Exception e) {
                Log.e(TAG, "Upload error: " + e.getMessage(), e);
                if (callback != null) callback.onError(e);
            }
        }).start();
    }
}
