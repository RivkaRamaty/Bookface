package com.example.kobishpak.hw01.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.kobishpak.hw01.model.Review;
import com.example.kobishpak.hw01.R;

import java.util.List;


public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private final String TAG = "ReviewsAdapter";

    private List<Review> reviewsList;

    public ReviewsAdapter(List<Review> reviewsList) {

        this.reviewsList = reviewsList;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.e(TAG,"onCreateViewHolder() >>");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);

        Log.e(TAG,"onCreateViewHolder() <<");
        return new ReviewViewHolder(parent.getContext(),itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {

        Log.e(TAG,"onBindViewHolder() >> " + position);

        Review review = reviewsList.get(position);

        holder.getUserName().setText(review.getUserName());
        holder.getUserReview().setText(review.getUserReview());
        holder.getUserRating().setRating(review.getUserRating());

        Log.e(TAG,"onBindViewHolder() << "+ position);
    }


    @Override
    public int getItemCount() {
        return reviewsList.size();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {

        private TextView userReview;
        private TextView userName;
        private RatingBar userRating;

        public ReviewViewHolder(Context context, View view) {

            super(view);
            userReview = view.findViewById(R.id.user_review);
            userName =  view.findViewById(R.id.user_name);
            userRating = view.findViewById(R.id.user_rating);

        }

        public TextView getUserReview() {
            return userReview;
        }

        public void setUserReview(TextView userReview) {
            this.userReview = userReview;
        }

        public TextView getUserName() {
            return userName;
        }

        public void setUserName(TextView userName) {
            this.userName = userName;
        }

        public RatingBar getUserRating() {
            return userRating;
        }

        public void setUserRating(RatingBar userRating) {
            this.userRating = userRating;
        }
    }
}
