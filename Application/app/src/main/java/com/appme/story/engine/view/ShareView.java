package com.appme.story.engine.view;

import android.content.res.ColorStateList;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import android.support.v4.view.ViewPager;
import android.support.design.widget.TabLayout;

import java.util.ArrayList;

import com.appme.story.application.ApplicationActivity;
import com.appme.story.engine.app.adapters.ShareViewPagerAdapter;
import com.appme.story.engine.app.utils.ViewUtil;
import com.appme.story.service.AppMeService;

public class ShareView extends LinearLayout {
    
    private ApplicationActivity activity;
    private AppMeService mAppMeService;
    private ViewPager viewPager;
    private ShareViewPagerAdapter shareViewPagerAdapter;

    public ShareView(ApplicationActivity activity, AppMeService mAppMeService) {
        super(activity);
        this.activity = activity;
        this.mAppMeService = mAppMeService;
        
        init();
        setOrientation(VERTICAL);
        viewPager.setAdapter(shareViewPagerAdapter);

        setUpTabLayout();

        addView(viewPager, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

    }

    private void init() {
        viewPager = new ViewPager(activity);
        shareViewPagerAdapter = new ShareViewPagerAdapter(activity, mAppMeService);
    }

    public void onDestroy() {
        shareViewPagerAdapter.onDestroy();
    }

    public void onResult(ArrayList<String> paths) {
        shareViewPagerAdapter.onResult(paths);
    }

    private void setUpTabLayout() {
        TabLayout tabLayout = new TabLayout(activity);
        tabLayout.setTabTextColors(ViewUtil.GREY_600, ViewUtil.BLUE_800);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        //tabLayout.setTabRippleColor(new ColorStateList(new int[][]{ new int[]{}},new int[]{ViewUtil.GREY_300}));
        tabLayout.setSelectedTabIndicatorColor(ViewUtil.BLUE_800);

        TabLayout.Tab uTabItem = tabLayout.newTab();
        uTabItem.setText("UPLOADS");
        TabLayout.Tab mTabItem = tabLayout.newTab();
        mTabItem.setText("MY FILES");

        tabLayout.addTab(uTabItem);
        tabLayout.addTab(mTabItem);
        tabLayout.setupWithViewPager(viewPager);
        addView(tabLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
}
