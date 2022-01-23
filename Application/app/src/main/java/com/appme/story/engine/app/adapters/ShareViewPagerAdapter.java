package com.appme.story.engine.app.adapters;

import android.view.View;
import android.view.ViewGroup;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;

import java.util.ArrayList;

import com.appme.story.application.ApplicationActivity;
import com.appme.story.engine.view.MyFilesView;
import com.appme.story.engine.view.UploadsView;
import com.appme.story.service.AppMeService;

public class ShareViewPagerAdapter extends PagerAdapter {
    private ApplicationActivity activity;
    private UploadsView uploadsView;
    private MyFilesView myFilesView;

    public ShareViewPagerAdapter(ApplicationActivity activity, AppMeService mAppMeService) {
        super();
        this.activity = activity;
        uploadsView = new UploadsView(activity, mAppMeService);
        myFilesView = new MyFilesView(activity, mAppMeService);
    }

    public void onResult(ArrayList<String> paths) {
        if (paths != null) {
            myFilesView.onResult(paths);
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "UPLOADS";
        } if (position == 1) {
            return "MY FILES";
        }
        return "";
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
//        if (position == 0) {
//            container.addView(settingsView);
//            return settingsView;
//        } else
        if (position == 0) {
            container.addView(uploadsView);
            return uploadsView;
        } else {
            container.addView(myFilesView);
            return myFilesView;
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    public void onDestroy() {
        myFilesView.onDestroy();
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
