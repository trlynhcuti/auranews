package com.example.auranews.utils;

import android.content.Context;
import android.net.Uri;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.InputStream;

public class WordUtils {

    public interface WordReadCallback {
        void onSuccess(String text);
        void onError(Exception e);
    }

    public static void readDocx(Context context, Uri uri, WordReadCallback callback) {
        new Thread(() -> {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                if (inputStream == null) throw new Exception("InputStream null");

                XWPFDocument document = new XWPFDocument(inputStream);
                StringBuilder text = new StringBuilder();
                for (XWPFParagraph para : document.getParagraphs()) {
                    text.append(para.getText()).append("\n");
                }
                inputStream.close();

                if (callback != null) callback.onSuccess(text.toString());
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        }).start();
    }
}
