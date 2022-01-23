package com.appme.story.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.appme.story.receiver.events.OnWifiStateChangedListener;

public class WifiStateChangeReceiver extends BroadcastReceiver {
    private OnWifiStateChangedListener onWifiStateChangedListener;

    public void setOnWifiStateChangedListener(OnWifiStateChangedListener onWifiStateChangedListener) {
        this.onWifiStateChangedListener = onWifiStateChangedListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        onWifiStateChangedListener.onWifiStateChanged(info != null && info.isConnected());
    }
}
