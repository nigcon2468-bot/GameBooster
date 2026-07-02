package com.gamebooster.overlay;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Create a simple, clean interface directly in code
        Button startButton = new Button(this);
        startButton.setText("LAUNCH GAME BOOSTER OVERLAY");
        startButton.setTextSize(18);
        startButton.setPadding(40, 40, 40, 40);
        
        setContentView(startButton);

        startButton.setOnClickListener(v -> {
            if (checkOverlayPermission()) {
                // Permission is already granted! Start the floating menu
                startService(new Intent(MainActivity.this, OverlayService.class));
                Toast.makeText(this, "Overlay Started! Open your game.", Toast.LENGTH_SHORT).show();
                finish(); // Close the main app window so it doesn't get in the way
            } else {
                // Ask for permission
                requestOverlayPermission();
            }
        });
    }

    // Checks if the phone allows this app to draw over other apps
    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true; // Older Android versions gave this permission automatically
    }

    // Opens the phone's system settings page directly to your app's toggle switch
    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    // Catches the moment the user comes back from the settings page
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (checkOverlayPermission()) {
                // User said yes! Fire up the service
                startService(new Intent(MainActivity.this, OverlayService.class));
                Toast.makeText(this, "Permission Granted! Overlay Active.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Permission denied. Overlay cannot start.", Toast.LENGTH_LONG).show();
            }
        }
    }
            }
            
