package com.appme.story.service;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.util.List;

import com.appme.story.AppController;
import com.appme.story.receiver.SendBroadcast;

public class ServiceMe {

    public static final String TAG = "ServiceMe";

    private static volatile ServiceMe Instance = null;

    private Context context;
    public static final int START_SERVICE = 0;
    public static final int START_ACTIVITY = 1;
    public static final int START_SERVER = 2;
    public static final int PAUSE_SERVICE = 3;
    public static final int RESUME_SERVICE = 4;
    public static final int NETWORK_STATUS = 5;
    public static final int SHUTDOWN_SERVICE = 6;
    public static final int OPEN_BROWSER = 7;
    private Intent mServiceIntent;
    public static ServiceMe getInstance() {
        ServiceMe localInstance = Instance;
        if (localInstance == null) {
            synchronized (ServiceMe.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new ServiceMe(AppController.getContext());
                }
            }
        }
        return localInstance;
    }

    private ServiceMe(Context context) {
        this.context = context;

    }

    public static ServiceMe with(Context context) {
        return new ServiceMe(context);
    }

    public void setIntent(Intent intent) {
        mServiceIntent = intent;   
    }

    public void setClass(Class<?> mClass) {
        mServiceIntent = new Intent(context, mClass);   
    }

    public void setAction(Integer status) {

        String action = "";      
        switch (status) {               
            case START_SERVER:
                action = SendBroadcast.ACTION.START_SERVER;
                break;
            case START_ACTIVITY:
                action = SendBroadcast.ACTION.START_ACTIVITY;
                break;      
            case PAUSE_SERVICE:
                action = SendBroadcast.ACTION.PAUSE_SERVICE;
                break;
            case RESUME_SERVICE:
                action = SendBroadcast.ACTION.RESUME_SERVICE;
                break;
            case SHUTDOWN_SERVICE:
                action = SendBroadcast.ACTION.SHUTDOWN_SERVICE;
                break; 
            case NETWORK_STATUS:
                action = SendBroadcast.ACTION.NETWORK_STATUS;
                break;
            case OPEN_BROWSER:
                action = SendBroadcast.ACTION.OPEN_BROWSER;
                break;    
        }
        mServiceIntent.setAction(action);          
    }

    public void setExtra(String message) {
        mServiceIntent.putExtra(SendBroadcast.EXTRA_SERVICE, message);
    }

    public void setExtra(String extra, String message) {
        mServiceIntent.putExtra(extra, message);
    }

    public void setExtra(String extra, int result) {
        mServiceIntent.putExtra(extra, result);
    }

    public void setExtra(String extra, int result, Intent intent) {
        mServiceIntent.putExtra(extra, result);
        mServiceIntent.putExtra(extra, intent);
    }

    public void startService() {
        context.startService(mServiceIntent);
    }

    public static void killAllServices(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo next : runningAppProcesses) {
            String processName = context.getPackageName() + ":service";
            if (next.processName.equals(processName)) {
                android.os.Process.killProcess(next.pid);
                break;
            }
        }
    }

    public static boolean isServiceRunning(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo next : runningAppProcesses) {
            String processName = context.getPackageName() + ":service";
            if (next.processName.equals(processName)) {
                return true;
            }
        }
        return false;
    }

    public void launchAppMeService(String message) {
        ServiceMe.killAllServices(context);
        Intent mServiceIntent = new Intent(context, AppMeService.class);
        mServiceIntent.putExtra(SendBroadcast.EXTRA_SERVICE, message);
        context.startService(mServiceIntent);
    }

    public void onStartServerService(String message) {
        Intent mServiceIntent = new Intent(context, AppMeService.class);
        mServiceIntent.setAction(SendBroadcast.ACTION.START_SERVER);
        mServiceIntent.putExtra(SendBroadcast.EXTRA_SERVICE, message);           
        context.startService(mServiceIntent);   
    }

    public void onPauseAppMeService() {
        if (isServiceRunning(context)) {
            Intent mServiceIntent = new Intent(context, AppMeService.class);
            mServiceIntent.setAction(SendBroadcast.ACTION.PAUSE_SERVICE);
            context.startService(mServiceIntent);
        }
    }

    public void onResumeAppMeService() {
        if (isServiceRunning(context)) {
            Intent mServiceIntent = new Intent(context, AppMeService.class);
            mServiceIntent.setAction(SendBroadcast.ACTION.RESUME_SERVICE);
            context.startService(mServiceIntent);
        }
    }

    public void onStopAppMeService() {
        if (isServiceRunning(context)) {
            Intent mServiceIntent = new Intent(context, AppMeService.class);
            mServiceIntent.setAction(SendBroadcast.ACTION.STOP_SERVICE);
            context.startService(mServiceIntent);
        }
    }

    public void onShutdownAppMeService() {
        if (isServiceRunning(context)) {
            Intent mServiceIntent = new Intent(context, AppMeService.class);
            mServiceIntent.setAction(SendBroadcast.ACTION.SHUTDOWN_SERVICE);
            context.startService(mServiceIntent);
        }
    }


    public enum Status {
        IDLE, 
        START_SERVICE,
        START_ACTIVITY, 
        PAUSE_SERVICE, 
        RESUME_SERVICE,
        STOP_SERVICE,
        OPEN_BROWSER,
        NETWORK_STATUS
        }

}
