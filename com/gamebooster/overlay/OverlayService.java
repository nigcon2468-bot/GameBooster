package com.gamebooster.overlay;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.app.ActivityManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private LinearLayout floatingMenu;
    private TextView ramTrackerText;
    private Handler handler = new Handler();
    private Runnable ramUpdater;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 1. Create the floating menu layout completely via code
        floatingMenu = new LinearLayout(this);
        floatingMenu.setOrientation(LinearLayout.VERTICAL);
        floatingMenu.setBackgroundColor(Color.parseColor("#1A1A2E")); // Dark gaming theme
        floatingMenu.setPadding(20, 20, 20, 20);

        // 2. Add the RAM tracker text to the menu
        ramTrackerText = new TextView(this);
        ramTrackerText.setTextColor(Color.WHITE);
        ramTrackerText.setTextSize(16);
        ramTrackerText.setText("RAM: Loading...");
        floatingMenu.addView(ramTrackerText);

        // 3. Add a Turbo Mode button
        Button turboButton = new Button(this);
        turboButton.setText("TURBO MODE");
        turboButton.setBackgroundColor(Color.parseColor("#E94560")); // Bright red neon
        turboButton.setTextColor(Color.WHITE);
        turboButton.setOnClickListener(v -> triggerTurboMode());
        floatingMenu.addView(turboButton);

        // 4. Setup layout rules so it hovers over EVERYTHING
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // The system overlay layer
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,     // Allows you to click the game behind it
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 100;

        // 5. Make the floating menu touch-responsive and draggable
        floatingMenu.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingMenu, params);
                        return true;
                }
                return false;
            }
        });

        // 6. Attach the layout to your screen
        windowManager.addView(floatingMenu, params);

        // 7. Start the loop to track RAM usage in real-time
        startRamTracking();
    }

    private void startRamTracking() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();

        ramUpdater = new Runnable() {
            @Override
            public void run() {
                activityManager.getMemoryInfo(memoryInfo);
                long availableMegs = memoryInfo.availMem / 1048576L; // Convert bytes to Megabytes
                long totalMegs = memoryInfo.totalMem / 1048576L;
                long usedMegs = totalMegs - availableMegs;

                ramTrackerText.setText("RAM: " + usedMegs + "MB / " + totalMegs + "MB");
                handler.postDelayed(this, 1000); // Refresh every 1 second
            }
        };
        handler.post(ramUpdater);
    }

    private void triggerTurboMode() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        // Basic un-rooted Turbo mode: Force clear background tasks to free up memory
        ramTrackerText.setText("Boosting...");
        am.killBackgroundProcesses("com.android.browser"); 
        // You can add list of common background lag apps here
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingMenu != null) windowManager.removeView(floatingMenu);
        handler.removeCallbacks(ramUpdater);
    }
  }

