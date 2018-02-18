package it.communikein.popularmovies;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import it.communikein.popularmovies.model.Review;
import it.communikein.popularmovies.databinding.DialogReviewBinding;

import static android.view.Window.FEATURE_NO_TITLE;

public class ShowReviewDialog extends DialogFragment {

    private DialogReviewBinding mBinding;

    private Review review;

    public ShowReviewDialog setReview(Review review) {
        this.review = review;

        return this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_review, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();

        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        // Call super onResume after sizing
        super.onResume();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(FEATURE_NO_TITLE);

        String content = mBinding.getRoot().getContext().getString(R.string.review_content, review.getContent());
        mBinding.contentTextview.setText(content);
        mBinding.nameTextview.setText(review.getAuthor());
    }
}
