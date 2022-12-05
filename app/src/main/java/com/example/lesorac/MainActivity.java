package com.example.lesorac;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.lesorac.activity.HomepageActivity;
import com.example.lesorac.activity.LoginActivity;
import com.example.lesorac.activity.OnboardingActivity;
import com.example.lesorac.util.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    private FirebaseAuth mFireAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SplashTheme);
        super.onCreate(savedInstanceState);

        sharedPref = getSharedPreferences("app_settings",MODE_PRIVATE);
        editor = sharedPref.edit();

        mFireAuth = FirebaseUtil.getAuth();

        if(sharedPref.getBoolean("onboard",false) && mFireAuth.getCurrentUser() != null){
            Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
            startActivity(intent);
            finish();
        }
        else if(sharedPref.getBoolean("onboard",false) && mFireAuth.getCurrentUser() == null){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Intent intent = new Intent(MainActivity.this, OnboardingActivity.class);
            startActivity(intent);
            finish();
        }

    }
}