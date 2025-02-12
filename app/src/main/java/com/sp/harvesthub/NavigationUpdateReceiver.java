package com.sp.harvesthub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.updateNavigationToHome();
        }
    }
}