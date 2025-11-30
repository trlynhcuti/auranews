package com.example.auranews.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.auranews.R;
import com.google.firebase.auth.FirebaseAuth; // Cần import
import com.google.firebase.auth.FirebaseUser; // Cần import

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        TextView textView = findViewById(R.id.tvSA);
        Animation fadeIn = new AlphaAnimation(0,1);
        fadeIn.setDuration(1000);
        textView.startAnimation(fadeIn);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                Intent intent;
                if (currentUser != null) {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }

                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}