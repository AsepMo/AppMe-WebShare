package com.appme.story.engine.view;

import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;

import com.appme.story.R;
import com.appme.story.application.ApplicationActivity;
import com.appme.story.engine.app.listeners.OnServiceBoundListener;
import com.appme.story.engine.app.adapters.ProgressAdapter;
import com.appme.story.engine.app.adapters.ProgressAdapterHelper;
import com.appme.story.engine.app.utils.ViewUtil;
import com.appme.story.service.AppMeService;
import com.appme.story.service.server.WebChatServer;

public class UploadsView extends RelativeLayout {
    
    private ApplicationActivity activity;
    private RecyclerView recyclerView;
    private ImageView nTextView;
    private ProgressAdapter adapter;
    public UploadsView(ApplicationActivity activity, AppMeService mAppMeService) {
        super(activity);
        this.activity = activity;
        
        init();

        setUpListeners(mAppMeService);

        setBackgroundResource(R.drawable.input_bg);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(activity));
        LinearLayout.LayoutParams rvLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(recyclerView, rvLayoutParams);

        nTextView.setImageResource(R.drawable.file_transfer);
//        nTextView.setText(R.string.no_files_transfers);
//        nTextView.setTextColor(ViewUtil.BLUE_800);
//        nTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        nTextView.setVisibility(GONE);
        RelativeLayout.LayoutParams tLayoutParams = new RelativeLayout.LayoutParams(ViewUtil.dp(200), ViewGroup.LayoutParams.WRAP_CONTENT);
        tLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        addView(nTextView, tLayoutParams);
    }

    private void init() {
        recyclerView = new RecyclerView(activity);
        nTextView = new ImageView(activity);

    }

    private void setUpListeners(AppMeService mAppMeService) {
        mAppMeService.addOnServiceBoundListener(new OnServiceBoundListener(){
                @Override
                public void onServiceBound(WebChatServer server) {
                    ProgressAdapterHelper progressAdapterHelper = new ProgressAdapterHelper(activity, server);
                    progressAdapterHelper.setOnNoFileTransfersListener(new ProgressAdapterHelper.OnNoFileTransfersListener() {
                            @Override
                            public void onNoFileTransfers(boolean isTrue) {
                                if (isTrue && nTextView.getVisibility() == GONE) {
                                    nTextView.setVisibility(VISIBLE);
                                } else {
                                    nTextView.setVisibility(GONE);
                                }
                            }
                        });
                    adapter = progressAdapterHelper.getProgressAdapter();
                    recyclerView.setAdapter(adapter);
                }
            });
    }

    
    class WrapContentLinearLayoutManager extends LinearLayoutManager {
        public WrapContentLinearLayoutManager(Context context) {
            super(context);
            setReverseLayout(true);
            setStackFromEnd(true);
        }


        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            final LinearSmoothScroller linearSmoothScroller =
                new LinearSmoothScroller(recyclerView.getContext()) {
                private static final float MILLISECONDS_PER_INCH = 200f;

                @Override
                public PointF computeScrollVectorForPosition(int targetPosition) {
                    return WrapContentLinearLayoutManager.this.computeScrollVectorForPosition(targetPosition);
                }

                @Override
                protected float calculateSpeedPerPixel
                (DisplayMetrics displayMetrics) {
                    return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                }
            };
            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
            }
        }
    }

}
