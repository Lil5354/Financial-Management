package com.example.expensetracker.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.R;
import com.example.expensetracker.ui.main.MainActivity;

/**
 * Splash Screen Activity
 * Hiển thị màn hình khởi động với logo và animation
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3 giây
    private static final int ANIMATION_DURATION = 1000; // 1 giây

    private ImageView ivLogo;
    private TextView tvAppName;
    private TextView tvTagline;
    private TextView tvLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initViews();
        setupAnimations();
        startSplashTimer();
    }

    /**
     * Khởi tạo các view
     */
    private void initViews() {
        ivLogo = findViewById(R.id.iv_logo);
        tvAppName = findViewById(R.id.tv_app_name);
        tvTagline = findViewById(R.id.tv_tagline);
        tvLoading = findViewById(R.id.tv_loading);
    }

    /**
     * Thiết lập animations cho các view
     */
    private void setupAnimations() {
        // Animation cho logo - fade in + scale
        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_animation);
        ivLogo.startAnimation(logoAnimation);

        // Animation cho tên app - slide up + fade in
        Animation appNameAnimation = AnimationUtils.loadAnimation(this, R.anim.app_name_animation);
        tvAppName.startAnimation(appNameAnimation);

        // Animation cho tagline - slide up + fade in (delay)
        Animation taglineAnimation = AnimationUtils.loadAnimation(this, R.anim.tagline_animation);
        tvTagline.startAnimation(taglineAnimation);

        // Animation cho loading text - fade in/out
        Animation loadingAnimation = AnimationUtils.loadAnimation(this, R.anim.loading_animation);
        tvLoading.startAnimation(loadingAnimation);
    }

    /**
     * Bắt đầu timer cho splash screen
     */
    private void startSplashTimer() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToMainActivity();
            }
        }, SPLASH_DELAY);
    }

    /**
     * Chuyển đến MainActivity
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        
        // Thêm transition animation
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        // Không cho phép back button trong splash screen
        super.onBackPressed();
    }
}
