package com.appme.story.engine.app.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import com.appme.story.application.ApplicationActivity;
import com.appme.story.service.server.loaders.FileData;
import com.appme.story.engine.view.LoaderView;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.FileViewHolder> {
    private final ArrayList<FileData> fileData;
    private int lastPosition = -1;
    private final ApplicationActivity activity;

    public ProgressAdapter(ApplicationActivity activity, ArrayList<FileData> fileData) {
        this.activity = activity;
        this.fileData = fileData;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(new LoaderView(activity));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileData fileData = this.fileData.get(position);
        holder.mainView.setFileData(fileData);

        setAnimation(holder.itemView, position);
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(view.getContext(), android.R.anim.fade_in);
            view.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return fileData.size();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        LoaderView mainView;

        public FileViewHolder(@NonNull LoaderView itemView) {
            super(itemView);
            mainView = itemView;
        }
    }


}
