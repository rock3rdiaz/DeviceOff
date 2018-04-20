package com.example.rdiazb.deviceoff;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private DevicePolicyManager deviceManger;
    private ActivityManager activityManager;
    private ComponentName compName;

    private Button btnLockScreen;
    private Button btnReboot;
    private Button btnDisableCamera;

    private static final int RESULT_ENABLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceManger = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);

        btnLockScreen = findViewById(R.id.btnLockScreen);
        btnLockScreen.setOnClickListener(v -> onLockScreen());

        btnReboot = findViewById(R.id.btnReboot);
        btnReboot.setOnClickListener(v -> onReboot());

        btnDisableCamera = findViewById(R.id.btnDisabledCamera);
        btnDisableCamera.setOnClickListener(v -> onDisableCamera());

    }

    private void onDisableCamera() {
        Log.i(TAG, "camera disabled!");
        boolean active = deviceManger.isAdminActive(compName);
        if (active) {
            deviceManger.setCameraDisabled(compName, true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
        i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Additional text explaining why this needs to be added.");
        startActivityForResult(i, RESULT_ENABLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Admin enabled!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Admin enabled FAILED!", Toast.LENGTH_LONG).show();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onLockScreen() {
        Log.i(TAG, "screen locked!");
        boolean active = deviceManger.isAdminActive(compName);
        if (active) {
            deviceManger.lockNow();
        }
    }

    private void onReboot() {
        boolean active = deviceManger.isAdminActive(compName);
        if (active) {
            try {
                if (deviceManger.isDeviceOwnerApp(getApplicationContext().getPackageName())
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    deviceManger.reboot(compName);
                } else {
                    Process p = Runtime.getRuntime().exec("ls /data");
                    p.waitFor();
                    if(p.exitValue() == 0)
                        Log.i(TAG, "command executed successfully!");
                    else
                        Log.i(TAG, "command has failed");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void showAdmins() {
        List<ComponentName> activeAdmins = deviceManger.getActiveAdmins();
        if(activeAdmins != null && !activeAdmins.isEmpty()){
            for(int index = 0; index < activeAdmins.size(); index++ ){
                Log.i(TAG, "flattenToShortString: "+ activeAdmins.get(index).flattenToShortString());
                Log.i(TAG, "flattenToString: "+ activeAdmins.get(index).flattenToString());
                Log.i(TAG, "getClassName: "+ activeAdmins.get(index).getClassName());
                Log.i(TAG, "getPackageName: "+ activeAdmins.get(index).getPackageName());
                Log.i(TAG, "getShortClassName: "+ activeAdmins.get(index).getShortClassName());
                Log.i(TAG, "toShortString: "+ activeAdmins.get(index).toShortString());
            }
        } else {
            Log.i(TAG, "No Active Device Policy Manager");
        }
    }

    private boolean isRooted() {
        boolean found = false;
        if (!found) {
            String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                    "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
            for (String where : places) {
                if ( new File( where + "su" ).exists() ) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }
}
