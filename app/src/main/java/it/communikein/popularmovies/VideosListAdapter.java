package it.communikein.popularmovies;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import it.communikein.popularmovies.databinding.ListItemVideoBinding;
import it.communikein.popularmovies.model.Video;
import it.communikein.popularmovies.VideosListAdapter.VideoViewHolder;

public class VideosListAdapter extends RecyclerView.Adapter<VideoViewHolder> {

    private List<Video> mList;

    @Nullable
    private final VideoClickCallback mOnClickListener;
    public interface VideoClickCallback {
        void onListVideoClick(Video video);
    }

    public VideosListAdapter(@Nullable VideoClickCallback listener) {
        this.mOnClickListener = listener;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListItemVideoBinding mBinding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.list_item_video,
                        parent,
                        false);

        return new VideoViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        Video video = mList.get(position);

        holder.bindData(video);
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }


    public void setList(final List<Video> newList) {
        final List<Video> tempList = new ArrayList<>(newList);

        if (mList == null) {
            mList = tempList;
            notifyItemRangeInserted(0, mList.size());
        }
        else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mList.size();
                }

                @Override
                public int getNewListSize() {
                    return tempList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mList.get(oldItemPosition).equals(tempList.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Video newItem = tempList.get(newItemPosition);
                    Video oldItem = mList.get(oldItemPosition);
                    return oldItem.displayEquals(newItem);
                }
            });
            mList = tempList;
            result.dispatchUpdatesTo(this);
        }
    }

    class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ListItemVideoBinding mBinding;

        VideoViewHolder(ListItemVideoBinding binding) {
            super(binding.getRoot());

            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setFocusable(true);

            this.mBinding = binding;
        }

        @Override
        public void onClick(View v) {
            Video clicked = mBinding.getVideo();

            if (mOnClickListener != null)
                mOnClickListener.onListVideoClick(clicked);
        }

        void bindData(Video video) {
            mBinding.setVideo(video);

            mBinding.videoTitleTextview.setText(video.getName());
        }
    }

}