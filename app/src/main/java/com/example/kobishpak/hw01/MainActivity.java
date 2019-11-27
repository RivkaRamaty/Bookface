package com.example.kobishpak.hw01;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN_TIME_OUT =3000;
    private FirebaseUser m_FirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_FirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                if (m_FirebaseUser != null) {
//                    Intent splashScreenIntent = new Intent(MainActivity.this, AllProductsActivity.class);
//                    startActivity(splashScreenIntent);
//                    finish();
//                } else {
                    Intent splashScreenIntent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(splashScreenIntent);
                    finish();
//                }
            }
        }, SPLASH_SCREEN_TIME_OUT);
    }
}
