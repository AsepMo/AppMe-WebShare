package com.appme.filepicker.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.appme.filepicker.FileGridItemView;
import com.appme.filepicker.LoadingView;
import com.appme.filepicker.data.FileInfo;
import com.appme.filepicker.R;
import com.appme.filepicker.adapter.events.DirClickListener;
import com.appme.filepicker.adapter.events.FileClickListener;
import com.appme.filepicker.utils.FileUtil;

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.FileGridItemViewHolder> {
    private FileInfo[] fileInfos;
    private int selectedCount;
    private DirClickListener dirClickListener;
    private FileClickListener fileClickListener;
    private int lastPosition = -1;

    public DirectoryAdapter(FileInfo[] fileInfos) {
        this.fileInfos = fileInfos;
        selectedCount = 0;
    }

    public void updateList(FileInfo[] fileInfos) {
        this.fileInfos = fileInfos;
        lastPosition = -1;
    }

    public void setDirClickListener(DirClickListener dirClickListener) {
        this.dirClickListener = dirClickListener;
    }

    public void setFileClickListener(FileClickListener fileClickListener) {
        this.fileClickListener = fileClickListener;
    }

    @NonNull
    @Override
    public FileGridItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileGridItemViewHolder(new FileGridItemView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull FileGridItemViewHolder holder, int position) {
        final FileInfo fileInfo = fileInfos[position];

        holder.mainView.setFileInfo(fileInfo);

        final float scaleFrom = 0.5f;
        final float scaleOriginal = 1f;
        final ImageView imageView = holder.mainView.getSelectImageView();

        holder.mainView.setBackgroundResource(fileInfo.isSelected() ? R.drawable.ripple_dir_item_selected : R.drawable.ripple_dir_item);

        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileInfo.isDirectory()) {
                    holder.mainView.getLoadingView().setVisibility(View.VISIBLE);
                    holder.mainView.getLoadingView().starAnimation();
                    if (dirClickListener != null) {
                        dirClickListener.onDirClicked(fileInfo);
                    }
                } else {
                    if (fileInfo.isSelected()) {
                        holder.mainView.setBackgroundResource(R.drawable.ripple_dir_item);

                        imageView.setScaleX(scaleOriginal);
                        imageView.setScaleY(scaleOriginal);

                        imageView.animate().scaleX(scaleFrom).scaleY(scaleFrom).setDuration(50).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                holder.mainView.getSelectImageView().setVisibility(View.GONE);
                            }
                        });

                        imageView.setScaleX(scaleFrom);
                        imageView.setScaleY(scaleFrom);

//                        holder.mainView.setBackgroundResource(R.drawable.stroke_card);
                        fileInfo.setSelected(false);
                    } else {
                        holder.mainView.setBackgroundResource(R.drawable.ripple_dir_item_selected);

                        imageView.setScaleX(scaleFrom);
                        imageView.setScaleY(scaleFrom);

                        imageView.setVisibility(View.VISIBLE);

                        imageView.animate().scaleX(scaleOriginal).scaleY(scaleOriginal).setDuration(50).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                imageView.setScaleX(scaleOriginal);
                                imageView.setScaleY(scaleOriginal);
                            }
                        });
//                        holder.mainView.setBackgroundResource(R.drawable.selected_stroke_card);

                        fileInfo.setSelected(true);
                    }
                    if (fileClickListener != null) {
                        fileClickListener.onFileClicked(fileInfo);
                    }
                }
            }
        });
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
        return fileInfos.length;
    }

    static class FileGridItemViewHolder extends RecyclerView.ViewHolder {
        FileGridItemView mainView;
        public FileGridItemViewHolder(@NonNull FileGridItemView itemView) {
            super(itemView);
            mainView = itemView;
        }
    }

}
