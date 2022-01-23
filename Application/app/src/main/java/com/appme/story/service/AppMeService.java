package com.appme.story.service;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.app.Service;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.Uri;
import android.media.ToneGenerator;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.RemoteViews;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.appme.story.R;
import com.appme.story.AppController;
import com.appme.story.application.ApplicationActivity;
import com.appme.story.service.server.WebChatServer;
import com.appme.story.receiver.SendBroadcast;
import com.appme.story.engine.app.utils.NetWorkUtils;
import com.appme.story.engine.app.listeners.OnServiceBoundListener;

public class AppMeService extends Service {

    public static String TAG = AppMeService.class.getSimpleName();  
    private static AppMeService foregroundService;

    private static final boolean DEBUG = false;

    private WindowManager mWindowManager;
    /**
     * AppMe Server.
     */
    private static WebChatServer server;
    private ArrayList<OnServiceBoundListener> onServiceBoundListeners;

    private final HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName(), android.os.Process.THREAD_PRIORITY_BACKGROUND);
    private Handler mHandler;
    private OnServiceBoundListener mOnServiceBoundListener;
    private Runnable mRunner = new Runnable(){
        @Override
        public void run() {      
            onServiceBoundListeners = new ArrayList<>();      
            server = new WebChatServer(getAssets());            
            if(mOnServiceBoundListener != null){
                mOnServiceBoundListener.onServiceBound(server);
            }
        }
    };

    private static boolean isRunning;
    private final ToneGenerator beeper = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
    private boolean isForeground = false;

    private RemoteViews mContentViewBig, mContentViewSmall;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new IllegalStateException("Binding not supported. Go away.");
    }

    public WindowManager getWindowManager() {
        return mWindowManager;
    }

    public Handler getHandler() {
        return mHandler;
    }

    @Override 
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.v(TAG, "onCreate:");
        foregroundService = this;

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        isRunning = true;
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        mHandler.postDelayed(mRunner, 1200);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.v(TAG, "onStartCommand:intent=" + intent);

        if (intent.getAction() == null) {
            String message = intent.getStringExtra(SendBroadcast.EXTRA_SERVICE);
            showNotification(message);             
            
            beeper.startTone(ToneGenerator.TONE_PROP_ACK);
            SendBroadcast.getInstance().broadcastStatus(SendBroadcast.SERVICE_IS_READY, message);           
            isRunning = true;

        } else if (SendBroadcast.ACTION.START_SERVER.equals(intent.getAction())) {
            String message = intent.getStringExtra(SendBroadcast.EXTRA_SERVICE);           
            if (server != null) {
                server.start(8080, message);
                showNotification(message);     
                SendBroadcast.getInstance().broadcastStatus(SendBroadcast.START_SERVER, "Server Is Started");                        
            } 
            beeper.startTone(ToneGenerator.TONE_PROP_ACK);                    
         } else if (SendBroadcast.ACTION.PAUSE_SERVICE.equals(intent.getAction())) { 
            if (server != null) {
                server.pause();
                 showNotification("Server Is Paused");     
                SendBroadcast.getInstance().broadcastStatus(SendBroadcast.PAUSE_SERVICE, "Server Is Paused");                        
            }
             beeper.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT);   
        } else if (SendBroadcast.ACTION.NETWORK_STATUS.equals(intent.getAction())) {       

        }  else if (SendBroadcast.ACTION.STOP_SERVICE.equals(intent.getAction())) {
            if (server != null) {
                server.stop();
                showNotification("Server Is Stopped");     
                SendBroadcast.getInstance().broadcastStatus(SendBroadcast.STOP_SERVICE, "Server Is Stoped");                         
            }
            beeper.startTone(ToneGenerator.TONE_PROP_BEEP);   
        } else if (SendBroadcast.ACTION.RESUME_SERVICE.equals(intent.getAction())) {
            if (server != null) {
                server.con();
                showNotification("Server Is Resumed");     
                SendBroadcast.getInstance().broadcastStatus(SendBroadcast.RESUME_SERVICE, "Server Is Continue");                  
            }
            beeper.startTone(ToneGenerator.TONE_PROP_ACK);   
        } else if (SendBroadcast.ACTION.SCREEN_SHOT_MONITOR.equals(intent.getAction())) {

        } else if (SendBroadcast.ACTION.OPEN_BROWSER.equals(intent.getAction())) {
            String mRootUrl = AppController.getServerIP();
            if (!TextUtils.isEmpty(mRootUrl)) {
                SendBroadcast.getInstance().broadcastStatus(SendBroadcast.OPEN_BROWSER, "Open Browser");                         
            } 


        } else if (SendBroadcast.ACTION.SHUTDOWN_SERVICE.equals(intent.getAction())) {
            beeper.startTone(ToneGenerator.TONE_PROP_NACK);

            SendBroadcast.getInstance().broadcastStatus(SendBroadcast.SERVICE_IS_SHUTDOWN, "Service Is Shutdown");                                 
            stopForeground(true);
            stopSelf();
        }

        return(START_NOT_STICKY);

    }

    public static boolean isRunning() {
        return server == null ? false : server.isRunning();
    }


    public WebChatServer getServer() {
        return server;
    }

    public void addOnServiceBoundListener(OnServiceBoundListener mOnServiceBoundListener){
        this.mOnServiceBoundListener = mOnServiceBoundListener;
    }
    
    /**
     * Show a notification while this service is running.
     */
    private void showNotification(String message) {
        NotificationManager mgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ApplicationActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification.setSmallIcon(R.drawable.apk_v2);  // the status icon
        notification.setWhen(System.currentTimeMillis());  // the time stamp
        notification.setContentIntent(contentIntent);  // The intent to send when the entry is clicked
        notification.setCustomContentView(getSmallContentView(message));
        notification.setCustomBigContentView(getBigContentView(message));
        notification.setPriority(NotificationCompat.PRIORITY_MAX);
        notification.setOngoing(true);

        Notification notif = notification.build();
        // Send the notification.
        if (isForeground) {
            mgr.notify(SendBroadcast.NOTIFY_ID, notif);
        } else {
            startForeground(SendBroadcast.NOTIFY_ID, notif);
            isForeground = true;
        }
        startForeground(SendBroadcast.NOTIFY_ID, notif);
    }

    private RemoteViews getSmallContentView(String message) {
        if (mContentViewSmall == null) {
            mContentViewSmall = new RemoteViews(getPackageName(), R.layout.remote_view_appme_small);
            setUpRemoteView(mContentViewSmall);
        }
        updateRemoteViews(mContentViewSmall, message);
        return mContentViewSmall;
    }

    private RemoteViews getBigContentView(String message) {
        if (mContentViewBig == null) {
            mContentViewBig = new RemoteViews(getPackageName(), R.layout.remote_view_appme);
            setUpRemoteView(mContentViewBig);
        }
        updateRemoteViews(mContentViewBig, message);
        return mContentViewBig;
    }

    private void setUpRemoteView(RemoteViews remoteView) {
        remoteView.setImageViewResource(R.id.image_view_close, R.drawable.ic_app_close);
        remoteView.setImageViewResource(R.id.image_view_play_last, R.drawable.ic_app_home);
        remoteView.setImageViewResource(R.id.image_view_play_next, R.drawable.ic_google_chrome);

        remoteView.setOnClickPendingIntent(R.id.button_close, buildPendingIntent(SendBroadcast.ACTION.SHUTDOWN_SERVICE));
        remoteView.setOnClickPendingIntent(R.id.button_play_last, buildPendingIntent(SendBroadcast.ACTION.START_ACTIVITY));
        remoteView.setOnClickPendingIntent(R.id.button_play_next, buildPendingIntent(SendBroadcast.ACTION.OPEN_BROWSER));
        remoteView.setOnClickPendingIntent(R.id.button_play_toggle, buildPendingIntent(SendBroadcast.ACTION.SCREEN_SHOT_MONITOR));
    }

    private void updateRemoteViews(RemoteViews remoteView, String message) {
        String serverIP = AppController.getServerIP();
        if (serverIP != null) {
            remoteView.setTextViewText(R.id.text_view_name, getString(R.string.appme_service));
            remoteView.setTextViewText(R.id.text_view_artist, message);       
        }
        remoteView.setImageViewResource(R.id.image_view_play_toggle, R.drawable.ic_monitor_screenshot);
        remoteView.setImageViewResource(R.id.image_view_album, R.drawable.apk_v2);
    }

    private void foregroundify() {
        NotificationManager mgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mgr.getNotificationChannel(SendBroadcast.CHANNEL_WHATEVER) == null) {
            mgr.createNotificationChannel(new NotificationChannel(SendBroadcast.CHANNEL_WHATEVER, "Whatever", NotificationManager.IMPORTANCE_DEFAULT));

        }

        NotificationCompat.Builder b = new NotificationCompat.Builder(this, SendBroadcast.CHANNEL_WHATEVER);
        b.setTicker(getString(R.string.app_name));    
        b.setWhen(System.currentTimeMillis());
        b.setAutoCancel(true);
        b.setDefaults(Notification.DEFAULT_ALL);
        b.setContentTitle(getString(R.string.appme_service));
        b.setContentText(getString(R.string.appme_service_is_running));
        b.setContentIntent(buildPendingIntent(SendBroadcast.START_ACTIVITY));
        b.setSmallIcon(R.mipmap.ic_launcher);
        b.addAction(R.drawable.ic_app_home, getString(R.string.appme_service_is_home), buildPendingIntent(SendBroadcast.ACTION.START_ACTIVITY));
        b.addAction(R.drawable.ic_google_chrome, getString(R.string.appme_start_browser), buildPendingIntent(SendBroadcast.ACTION.OPEN_BROWSER));

        if (isForeground) {
            mgr.notify(SendBroadcast.NOTIFY_ID, b.build());
        } else {
            startForeground(SendBroadcast.NOTIFY_ID, b.build());
            isForeground = true;
        }
        startForeground(SendBroadcast.NOTIFY_ID, b.build());
    }



    // Private methods
    /*private Notification getNotificationStart() {
     final Intent mainActivityIntent = new Intent(this, ApplicationActivity.class);
     //mainActivityIntent.setAction(ApplicationActivity.ACTION_SCREEN_SERVER);
     mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     final PendingIntent pendingMainActivityIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, 0);

     final NotificationCompat.Builder startNotificationBuilder = new NotificationCompat.Builder(this);
     startNotificationBuilder.setSmallIcon(R.drawable.apk_v2);
     startNotificationBuilder.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
     startNotificationBuilder.setContentTitle(getResources().getString(R.string.service_ready_to_stream));
     startNotificationBuilder.setContentText(getResources().getString(R.string.service_press_start));
     startNotificationBuilder.setContentIntent(pendingMainActivityIntent);
     startNotificationBuilder.addAction(R.drawable.ic_app_home, getResources().getString(R.string.service_start), PendingIntent.getBroadcast(this, 0, startStreamIntent, 0));
     startNotificationBuilder.addAction(R.drawable.ic_app_close, getResources().getString(R.string.service_close), PendingIntent.getBroadcast(this, 0, closeIntent, 0));
     startNotificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
     return startNotificationBuilder.build();
     }

     private Notification getNotificationStop() {
     final Intent mainActivityIntent = new Intent(this, ApplicationActivity.class);
     //mainActivityIntent.setAction(MonitorActivity.ACTION_SCREEN_SERVER);
     mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     final PendingIntent pendingMainActivityIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, 0);

     final NotificationCompat.Builder stopNotificationBuilder = new NotificationCompat.Builder(this);
     stopNotificationBuilder.setSmallIcon(R.drawable.apk_v2);
     stopNotificationBuilder.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
     stopNotificationBuilder.setContentTitle(getResources().getString(R.string.service_stream));
     stopNotificationBuilder.setContentText(getResources().getString(R.string.service_go_to) + AppController.getServerIP());
     stopNotificationBuilder.setContentIntent(pendingMainActivityIntent);
     stopNotificationBuilder.addAction(R.drawable.ic_stop, getResources().getString(R.string.service_stop), PendingIntent.getBroadcast(this, 0, stopStreamIntent, 0));
     stopNotificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
     return stopNotificationBuilder.build();
     }*/

    // PendingIntent
    private PendingIntent getPendingIntent(String action) {
        return PendingIntent.getService(this, 0, new Intent(action), 0);
    }

    private PendingIntent buildPendingIntent(String action) {
        Intent i = new Intent(this, getClass());
        i.setAction(action);
        return(PendingIntent.getService(this, 0, i, 0));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();     
        //foregroundServiceTaskHandler.getLooper().quit();
        stopForeground(true);  
        //stopServer(); // Stop server.        
    } 
}
