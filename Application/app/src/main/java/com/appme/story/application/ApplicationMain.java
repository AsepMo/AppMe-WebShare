package com.appme.story.application;

import android.app.Application;
import android.content.Context;

import java.io.File;
import java.io.IOException;


public class ApplicationMain extends Application {
    
    public static final String TAG = "ApplicationMain";
    private static final String APPME_FILE_NAME = "appMe.dat";
    
    private static volatile Context mContext;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        mContext = this;
        
        initAnalytics();
        initCrashHandler();
        initConfig();
        initFolder();
        initSoundManager();
    }
    
    public static synchronized Context getContext(){
        return mContext;
    }
    
    public void initAnalytics(){}
    public void initCrashHandler(){}
    public void initConfig(){}
    public void initFolder(){}
    public void initSoundManager(){}
    
    
}
