package com.example.auranews.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.auranews.R;

public class NoInternetActivity extends AppCompatActivity {

    private static NoInternetActivity instance;

    public static boolean isRunning() {
        return instance != null;
    }

    public static void finishInstance() {
        if (instance != null) {
            instance.finish();
            instance = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);
        instance = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }
}

