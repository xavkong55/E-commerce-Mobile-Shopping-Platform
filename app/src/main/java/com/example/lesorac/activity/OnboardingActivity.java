package com.example.lesorac.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.lesorac.R;
import com.example.lesorac.adapter.SliderAdapter;

public class OnboardingActivity extends AppCompatActivity {

    ViewPager slideViewPager;
    LinearLayout dotLayout;

    private TextView[] mDots;
    private Button prev_button , next_button;
    private SliderAdapter sliderAdapter;
    private int currentPage;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        slideViewPager = findViewById(R.id.slideViewPager);
        dotLayout = findViewById(R.id.onboard_dot);
        prev_button = findViewById(R.id.onboard_prev);
        next_button = findViewById(R.id.onboard_next);

        sliderAdapter = new SliderAdapter(this);
        slideViewPager.setAdapter(sliderAdapter);

        addDotsIndicator(0);
        slideViewPager.addOnPageChangeListener(viewListener);

        sharedPref = getSharedPreferences("app_settings",MODE_PRIVATE);
        editor = sharedPref.edit();

        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(currentPage == 2){

                    editor.putBoolean("onboard",true);
                    editor.commit();
                    Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    slideViewPager.setCurrentItem(currentPage + 1);
                }
            }
        });


        prev_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideViewPager.setCurrentItem(currentPage - 1);
            }
        });
    }

    public void addDotsIndicator(int position){
        mDots = new TextView[3];
        dotLayout.removeAllViews();
        for(int i = 0; i < mDots.length; i++){
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.light_grey));

            dotLayout.addView(mDots[i]);

        }

        if(mDots.length > 0){
            mDots[position].setTextColor(getResources().getColor(R.color.black));
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);
            currentPage = position;

            if(position == 0){
                next_button.setEnabled(true);
                prev_button.setEnabled(false);
                prev_button.setVisibility(View.INVISIBLE);

                next_button.setText("Next");
                prev_button.setText("Bac");
            }
            else if (position == mDots.length - 1){

                next_button.setEnabled(true);
                prev_button.setEnabled(true);
                prev_button.setVisibility(View.VISIBLE);

                next_button.setText("Finish");
                prev_button.setText("back");
            }
            else{
                next_button.setEnabled(true);
                prev_button.setEnabled(true);
                prev_button.setVisibility(View.VISIBLE);

                next_button.setText("Next");
                prev_button.setText("back");
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}