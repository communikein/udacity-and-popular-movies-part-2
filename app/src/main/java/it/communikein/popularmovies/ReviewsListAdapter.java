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

import it.communikein.popularmovies.ReviewsListAdapter.ReviewViewHolder;
import it.communikein.popularmovies.databinding.ListItemReviewBinding;
import it.communikein.popularmovies.model.Review;

public class ReviewsListAdapter extends RecyclerView.Adapter<ReviewViewHolder> {

    private List<Review> mList;

    @Nullable
    private final ReviewClickCallback mOnClickListener;
    public interface ReviewClickCallback {
        void onReviewClick(Review review);
    }

    public ReviewsListAdapter(@Nullable ReviewClickCallback listener) {
        this.mOnClickListener = listener;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListItemReviewBinding mBinding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.list_item_review,
                        parent,
                        false);

        return new ReviewViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        Review review = mList.get(position);

        holder.bindData(review);
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }


    public void setList(final List<Review> newList) {
        final List<Review> tempList = new ArrayList<>(newList);

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
                    Review newItem = tempList.get(newItemPosition);
                    Review oldItem = mList.get(oldItemPosition);
                    return oldItem.displayEquals(newItem);
                }
            });
            mList = tempList;
            result.dispatchUpdatesTo(this);
        }
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ListItemReviewBinding mBinding;

        ReviewViewHolder(ListItemReviewBinding binding) {
            super(binding.getRoot());

            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setFocusable(true);

            this.mBinding = binding;
        }

        @Override
        public void onClick(View v) {
            Review clicked = mBinding.getReview();

            if (mOnClickListener != null)
                mOnClickListener.onReviewClick(clicked);
        }

        void bindData(Review review) {
            mBinding.setReview(review);

            String content = mBinding.getRoot().getContext()
                    .getString(R.string.review_content, review.getContent());
            mBinding.contentTextview.setText(content);
            mBinding.nameTextview.setText(review.getAuthor());
        }
    }

}