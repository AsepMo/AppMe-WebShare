package com.appme.story.application;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.content.ContextCompat;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appme.filepicker.FilePickerActivity;

import java.util.ArrayList;

import com.appme.story.R;
import com.appme.story.engine.app.dialogs.WritePermissionDialog;
import com.appme.story.engine.app.utils.NetWorkUtils;
import com.appme.story.engine.app.utils.ViewUtil;
import com.appme.story.engine.app.listeners.OnIpAddressChangedListener;
import com.appme.story.engine.app.tasks.Executor;
import com.appme.story.engine.view.MyFilesView;
import com.appme.story.engine.view.ServerView;
import com.appme.story.engine.view.ShareView;
import com.appme.story.receiver.AppMeReceiver;
import com.appme.story.receiver.SendBroadcast;
import com.appme.story.receiver.HotspotStateChangeReceiver;
import com.appme.story.receiver.WifiStateChangeReceiver;
import com.appme.story.receiver.events.OnWifiStateChangedListener;
import com.appme.story.receiver.events.OnHotspotChangedListener;
import com.appme.story.service.AppMeService;
import com.appme.story.service.server.WebChatServer;
import com.appme.story.service.server.loaders.Downloader;
import com.appme.story.service.server.loaders.Uploader;
import com.appme.story.service.server.utils.FileUtil;
import com.appme.story.service.ServiceMe;


public class ApplicationActivity extends AppCompatActivity implements AppMeReceiver.OnSendBroadcastListener  {
     
    private TextView uSpeedTextView, dSpeedTextView;
    private RelativeLayout relativeLayout;
    private ImageView serverImageView, shareImageView;
    private TextView serverTextView, shareTextView;
    private ServerView serverView;
    private ShareView shareView;
    //private HTTPServer server;
    private AppMeService serverService;
    private WifiStateChangeReceiver wifiStateChangeReceiver;
    private HotspotStateChangeReceiver hotspotStateChangeReceiver;
    private OnIpAddressChangedListener onIpAddressChangedListener;
    
    private Executor executor;
    private String currentIp;
    private boolean isWifi, isHotspot;
    
    public static final int PERMISSION_REQUEST_CODE = 203;
    private AppMeReceiver mAppMeReceiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ViewUtil.init(this);
        uSpeedTextView = (TextView)findViewById(R.id.us_tv);
        dSpeedTextView = (TextView)findViewById(R.id.ds_tv);
        relativeLayout = (RelativeLayout)findViewById(R.id.rl);
        
        serverImageView = (ImageView)findViewById(R.id.server_iv);
        serverTextView = (TextView)findViewById(R.id.server_tv);
        shareImageView = (ImageView)findViewById(R.id.share_iv);
        shareTextView = (TextView)findViewById(R.id.share_tv);
        wifiStateChangeReceiver = new WifiStateChangeReceiver();
        hotspotStateChangeReceiver = new HotspotStateChangeReceiver();
        
        serverService = new AppMeService();
        serverView = new ServerView(this, serverService);
        shareView = new ShareView(this, serverService);
        
        shareView.setVisibility(View.GONE);
        relativeLayout.addView(serverView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        relativeLayout.addView(shareView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        checkPermissions();
        registerBroadcastReceivers();
          
       if (!isServerServiceRunning()) {
            ServiceMe.getInstance().launchAppMeService("Service Is Running");
        }else{
            ServiceMe.getInstance().onStartServerService("Server Is Running");
        }
        
        mAppMeReceiver = new AppMeReceiver();
        mAppMeReceiver.setOnSendBroadcastListener(this);
        registerAppMeBroadcastReceiver();
        
    }
    
    public void registerAppMeBroadcastReceiver() {
        IntentFilter statusIntentFilter = new IntentFilter(SendBroadcast.PROCESS_BROADCAST_ACTION);
        registerReceiver(mAppMeReceiver, statusIntentFilter);
    }
    
    @Override
    public void onServiceReady(String message) {
        showToast(message); 
        setUpExecutor();
        
        
    }

    @Override
    public void onStartServer(String message) {
        showToast(message);
    }

    @Override
    public void onStartActivity(String message) {
        showToast(message);
    }

    @Override
    public void onNetworkStatus(String message) {
        showToast(message);
    }

    @Override
    public void onOpenBrowser(String message) {
        showToast(message);
    }

    @Override
    public void onPauseService(String message) {
        showToast(message);
    }

    @Override
    public void onResumeService(String message) {
        showToast(message);
    }

    @Override
    public void onStopService(String message) {
        showToast(message);
    }

    @Override
    public void onServiceShutDown(String message) {
        showToast(message);
    }
    
    public void showToast(String message){
        Toast.makeText(ApplicationActivity.this, message, Toast.LENGTH_SHORT).show();
    }
    
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new WritePermissionDialog(this).show();
//            } else {
//                new WritePermissionDialog(this).show();
//            }
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (!Environment.isExternalStorageManager()) {
//                new PermissionDialog(this).show();
//            }
//        }
    }
    
    private boolean isServerServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AppMeService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void setUpExecutor() {
        
        if (executor == null) {
            executor = new Executor(this, new Runnable() {
                    @Override
                    public void run() {
                        uSpeedTextView.setText(String.format("%s/s", FileUtil.getSize(serverService.getServer().getUploadSpeed())));
                        dSpeedTextView.setText(String.format("%s/s", FileUtil.getSize(serverService.getServer().getDownloadSpeed())));
                    }
                }, 1000);
            executor.start();
        }
    }

    private void registerBroadcastReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiStateChangeReceiver.setOnWifiStateChangedListener(new OnWifiStateChangedListener(){
                @Override
                public void onWifiStateChanged(boolean connected){
                    isWifi = connected;
                    if (isWifi) {
                        currentIp = NetWorkUtils.getWifiIpAddress();
                    } else if (isHotspot) {
                        currentIp = NetWorkUtils.getHotspotIpAddress();
                    } else {
                        currentIp = NetWorkUtils.getLocalHostIpAddress();
                    }
                    if (onIpAddressChangedListener != null) {
                        onIpAddressChangedListener.onIpAddressChanged(currentIp);
                    }
                }
            });
        registerReceiver(wifiStateChangeReceiver, intentFilter);

        IntentFilter intentFilter1 = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED");
        hotspotStateChangeReceiver.setOnHotspotChangedListener(new OnHotspotChangedListener(){
                @Override
                public void onHotspotChanged(boolean on){
                    isHotspot = on;
                    String ip = null;
                    if (on) {
                        while (ip == null) {
                            ip = NetWorkUtils.getHotspotIpAddress();
                        }
                    }
                    if (isHotspot) {
                        currentIp = ip;
                    } else if (isWifi) {
                        currentIp = NetWorkUtils.getWifiIpAddress();
                    } else {
                        currentIp = NetWorkUtils.getLocalHostIpAddress();
                    }
                    
                    if (onIpAddressChangedListener != null) {
                        onIpAddressChangedListener.onIpAddressChanged(currentIp);
                    }
                }
            });
        registerReceiver(hotspotStateChangeReceiver, intentFilter1);      
    }

    
    private void unregisterBroadcastReceivers() {
        unregisterReceiver(wifiStateChangeReceiver);
        unregisterReceiver(hotspotStateChangeReceiver);
        unregisterReceiver(mAppMeReceiver); 
    }

    

    public void setOnIpAddressChangedListener(OnIpAddressChangedListener onIpAddressChangedListener) {
        this.onIpAddressChangedListener = onIpAddressChangedListener;
        onIpAddressChangedListener.onIpAddressChanged(currentIp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ArrayList<String> paths = new ArrayList<>();
            if (requestCode == MyFilesView.CUSTOM_FILE_PICKET_REQUEST_CODE) {
                paths = data.getStringArrayListExtra(FilePickerActivity.PATH_LIST_KEY);
            }
//            else if (requestCode == ShareView.DEFAULT_FILE_PICKET_REQUEST_CODE) {
//                if(null != data) { // checking empty selection
//                    ClipData cd = data.getClipData();
//                    if(null != cd) { // checking multiple selection or not
//                        int len = cd.getItemCount();
//                        for (int j = 0; j < len; ++j) {
//                            try {
//                                Log.i("TAG_URI", "onActivityResult: " +  cd.getItemAt(j).getUri().toString());
//                                addPath(data, paths, cd.getItemAt(j).getUri());
//                            } catch (IllegalArgumentException e) {
//                                Log.i("TAG_URI_NOT_FOUND", "onActivityResult: " + e.getMessage());
//                            }
//                        }
//                    } else {
//                        try {
//                            Log.i("TAG_URI", "onActivityResult: " + data.getData().toString());
//                            addPath(data, paths, data.getData());
//                        } catch (IllegalArgumentException e) {
//                            Log.i("TAG_URI_NOT_FOUND", "onActivityResult: " + e);
//                        }
//                    }
//                }
//            }
            shareView.onResult(paths);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.v(TAG, "onResume:");  
        registerAppMeBroadcastReceiver();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();     
        unregisterBroadcastReceivers();
        shareView.onDestroy();       
    }

    public void toggle() {
        Intent intent = new Intent();
        intent.setClass(ApplicationActivity.this, AppMeService.class);
        if (!AppMeService.isRunning()) {
            startService(intent);
        } else {
            stopService(intent);
        }
    }
    
    public void server(View view) {
        shareView.setVisibility(View.GONE);
        serverView.setVisibility(View.VISIBLE);
        serverImageView.setImageTintList(ColorStateList.valueOf(ViewUtil.WHITE));
        serverTextView.setTextColor(ViewUtil.WHITE);
        shareImageView.setImageTintList(ColorStateList.valueOf(ViewUtil.BLUE_300));
        shareTextView.setTextColor(ViewUtil.BLUE_300);
    }

    public void share(View view) {
        serverView.setVisibility(View.GONE);
        shareView.setVisibility(View.VISIBLE);
        serverImageView.setImageTintList(ColorStateList.valueOf(ViewUtil.BLUE_300));
        serverTextView.setTextColor(ViewUtil.BLUE_300);
        shareImageView.setImageTintList(ColorStateList.valueOf(ViewUtil.WHITE));
        shareTextView.setTextColor(ViewUtil.WHITE);
    }
    
    
}
