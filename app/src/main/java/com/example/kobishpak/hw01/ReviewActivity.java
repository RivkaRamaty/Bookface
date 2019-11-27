package com.example.kobishpak.hw01;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBar;
import com.example.kobishpak.hw01.model.Book;
import com.example.kobishpak.hw01.model.Review;
import com.example.kobishpak.hw01.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class ReviewActivity extends Activity {

    private final String TAG = "ReviewActivity";
    private Book book;
    private String key;
    private User user;
    private int prevRating = -1;

    private TextView userReview;
    private RatingBar userRating;
    private DatabaseReference bookRef;
    private AnalyticsManager m_AnalyticsManager = AnalyticsManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.e(TAG, "onCreate() >>");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        key = getIntent().getStringExtra("key");
        book = getIntent().getParcelableExtra("book");
        user = (User)getIntent().getSerializableExtra("user");

        userReview = findViewById(R.id.new_user_review);
        userRating = findViewById(R.id.new_user_rating);
        m_AnalyticsManager.init(this);

        bookRef = FirebaseDatabase.getInstance().getReference("Books/" + key);

        bookRef.child("/reviews/" +  FirebaseAuth.getInstance().getCurrentUser().getUid()).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        Log.e(TAG, "onDataChange(Review) >> " + snapshot.getKey());

                        Review review = snapshot.getValue(Review.class);
                        if (review != null) {
                            userReview.setText(review.getUserReview());
                            userRating.setRating(review.getUserRating());
                            prevRating = review.getUserRating();
                        }

                        Log.e(TAG, "onDataChange(Review) <<");

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Log.e(TAG, "onCancelled(Review) >>" + databaseError.getMessage());
                    }
                });

        Log.e(TAG, "onCreate() <<");

    }

    public void onSubmitClick(View v) {

        Log.e(TAG, "onSubmitClick() >>");


        bookRef.runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                Log.e(TAG, "doTransaction() >>" );


                Book book = mutableData.getValue(Book.class);

                if (book == null ) {
                    Log.e(TAG, "doTransaction() << book is null" );
                    return Transaction.success(mutableData);
                }

                if (prevRating == -1) {
                    // Increment the review count and rating only in case the user enters a new review
                    book.incrementReviewCount();
                    book.incrementRating((int)userRating.getRating());
                } else{
                    book.incrementRating((int)userRating.getRating() - prevRating);
                }

                mutableData.setValue(book);
                Log.e(TAG, "doTransaction() << book was set");
                return Transaction.success(mutableData);

            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {

                Log.e(TAG, "onComplete() >>" );

                if (databaseError != null) {
                    Log.e(TAG, "onComplete() << Error:" + databaseError.getMessage());
                    return;
                }

                if (committed) {
                    Review review = new Review(
                            userReview.getText().toString(),
                            (int)userRating.getRating(),
                            user.getName());

                    bookRef.child("/reviews/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(review);
                    m_AnalyticsManager.trackBookRating(book, (int)userRating.getRating());
                }


                Intent intent = new Intent(getApplicationContext(),BookDetailsActivity.class);
                intent.putExtra("book", book);
                intent.putExtra("key", key);
                intent.putExtra("user",user);
                startActivity(intent);
                finish();

                Log.e(TAG, "onComplete() <<" );
            }
        });



        Log.e(TAG, "onSubmitClick() <<");
    }

}

