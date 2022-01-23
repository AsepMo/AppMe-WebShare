package com.appme.story;

import java.util.ArrayList;
import java.util.List;

import com.appme.story.application.ApplicationMain;
import com.appme.story.application.ApplicationSettings;
import com.appme.story.engine.app.analytics.CrashHandler;
import com.appme.story.engine.app.utils.NetWorkUtils;

public class AppController extends ApplicationMain {

    private static AppController sAppController;
    private ApplicationSettings applicationSettings;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        sAppController = this;
        applicationSettings = new ApplicationSettings(this);
    }

    public static AppController getAppController() {
        return sAppController;
    }

    @Override
    public void initAnalytics() {
        super.initAnalytics();
    }

    @Override
    public void initCrashHandler() {
        super.initCrashHandler();
		CrashHandler.init(this);
    }

    @Override
    public void initConfig() {
        super.initConfig();
        
    }

    @Override
    public void initFolder() {
        super.initFolder();
    }

    @Override
    public void initSoundManager() {
        super.initSoundManager();
                
    }
	
    public static String getServerIP() {
        return "http://" + NetWorkUtils.getLocalIpAddress() + ":" + sAppController.applicationSettings.getServerPort();
    }
    
    
}
