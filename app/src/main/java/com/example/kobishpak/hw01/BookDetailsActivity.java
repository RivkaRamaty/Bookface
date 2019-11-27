package com.example.kobishpak.hw01;

import android.Manifest;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kobishpak.hw01.adapter.ReviewsAdapter;
import com.example.kobishpak.hw01.model.Book;
import com.example.kobishpak.hw01.model.Review;
import com.example.kobishpak.hw01.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BookDetailsActivity extends AppCompatActivity {

    public final String TAG = "BookDetailsActivity";
    private Book book;
    private String key;
    private User user;
    private int price;
    private Button buy;
    private Button mReviewButton;
    private RecyclerView recyclerViewBookReviews;

    private DatabaseReference bookReviewsRef;
    private StorageReference mBookStorage;
    private List<Review> reviewsList = new ArrayList<>();
    private AnalyticsManager m_AnalyticsManager = AnalyticsManager.getInstance();

    private boolean bookWasPurchased;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.e(TAG, "onCreate() >>");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        key = getIntent().getStringExtra("key");
        book = getIntent().getParcelableExtra("book");
        user = (User)getIntent().getSerializableExtra("user");
        mBookStorage = FirebaseStorage.getInstance().getReference().child(("Books/"));
        m_AnalyticsManager.init(this);
        m_AnalyticsManager.trackBookEvent("book_viewed", book);

        StorageReference thumbRef = FirebaseStorage
                .getInstance()
                .getReference()
                .child("Thumbs/" + book.getThumbImage());

        thumbRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                // Load the image using Glide
                Glide.with(BookDetailsActivity.this)
                        .load(uri)
                        .into((ImageView) findViewById(R.id.imageViewBook));
                Log.e(TAG, "DownloadCurrentBook() << SUCCESS");
            }
        });

        ((TextView) findViewById(R.id.textViewName)).setText(book.getName());
        ((TextView) findViewById(R.id.textViewArtist)).setText(book.getArtist());
        ((TextView) findViewById(R.id.textViewGenre)).setText(book.getGenre());
        buy = findViewById(R.id.buttonBuy);
        if (user.getSignupMethod().equals("anonymousUser"))
        {
            Toast.makeText(BookDetailsActivity.this, "In order to purchase a Book, you must log in first.",Toast.LENGTH_LONG).show();
            buy.setText("Login");
        }
        else {
            price = book.getPrice() - getIntent().getIntExtra("discount",0);
            buy.setText("BUY $" + price);
        }
        Iterator i = user.getMyBooks().iterator();
        while (i.hasNext()) {
            if (i.next().equals(key)) {
                bookWasPurchased = true;
                buy.setText(R.string.download);
                break;
            }
        }

        buy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Log.e(TAG, "buy.onClick() >> file=" + book.getName());

                if (!user.getSignupMethod().equals("anonymousUser")) { ///////////////////
                    if (bookWasPurchased) {
                        Log.e(TAG, "buy.onClick() >> Downloading purchased book");
                        //User purchased the book so he can download it
                        haveStoragePermission();
                        Toast.makeText(BookDetailsActivity.this,"Downloading Please wait..",Toast.LENGTH_LONG).show();

                        downloadCurrentBook(book.getFile());
                        logBookEvent("book_downloaded");

                    } else {
                        //Purchase the book.
                        Log.e(TAG, "buy.onClick() >> Purchase the book");
                        user.getMyBooks().add(key);
                        user.updatePurchaseStatus(price);
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user);
                        bookWasPurchased = true;
                        buy.setText(R.string.download);
                        String title = "Congratulations on your new book purchase!",
                                body = "Check out other books that you might enjoy!",
                                channelId = "fcm_default_channel";
                        Intent intent = new Intent(BookDetailsActivity.this, AllProductsActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(BookDetailsActivity.this, 0, intent,0);
                        NotificationCompat.Builder notificationBuilder =
                                new NotificationCompat.Builder(BookDetailsActivity.this, null)
                                        .setContentTitle(title)
                                        .setAutoCancel(true)
                                        .setContentIntent(pendingIntent)
                                        .setContentText(body)
                                        .setSmallIcon(R.drawable.ic_launcher)
                                        .setChannelId(channelId);

                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(0,notificationBuilder.build());
                        m_AnalyticsManager.trackPurchase(book);
                        m_AnalyticsManager.setUserProperty("total_purchase",Integer.toString(user.getTotalPurchase()));
                        m_AnalyticsManager.setUserProperty("my_books_count",Integer.toString(user.getMyBooksCount()));
                    }
                    Log.e(TAG, "DownloadBook.onClick() <<");
                }
                else
                {
                    startActivity(new Intent(BookDetailsActivity.this,LoginActivity.class));
                }}
            });

        recyclerViewBookReviews = findViewById(R.id.book_reviews);
        recyclerViewBookReviews.setHasFixedSize(true);
        recyclerViewBookReviews.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerViewBookReviews.setItemAnimator(new DefaultItemAnimator());


        ReviewsAdapter reviewsAdapter = new ReviewsAdapter(reviewsList);
        recyclerViewBookReviews.setAdapter(reviewsAdapter);

        bookReviewsRef = FirebaseDatabase.getInstance().getReference("Books/" + key +"/reviews");

        bookReviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Log.e(TAG, "onDataChange() >> Books/" + key);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Review review = dataSnapshot.getValue(Review.class);
                    reviewsList.add(review);
                }
                recyclerViewBookReviews.getAdapter().notifyDataSetChanged();
                Log.e(TAG, "onDataChange(Review) <<");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.e(TAG, "onCancelled(Review) >>" + databaseError.getMessage());
            }
        });
        Log.e(TAG, "onCreate() <<");

        mReviewButton = findViewById(R.id.button_review);

        mReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookDetailsActivity.this, ReviewActivity.class);
                intent.putExtra("book", book);
                intent.putExtra("key", key);
                intent.putExtra("user",user);
                startActivity(intent);
            }
        });
    }

    private void logBookEvent(String event){
        m_AnalyticsManager.trackBookAcquiredEvent(event, book);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void downloadCurrentBook(String bookFile) {

        StorageReference BookReference = mBookStorage.child(bookFile);
        Log.e(TAG, "DownloadCurrentBook() >> bookFile=" + BookReference);

        final String bookName = bookFile;
        BookReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                saveFile(uri, bookName);
                Log.e(TAG, "DownloadCurrentBook() << SUCCESS");
            }
        });
    }

    public  boolean haveStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission error","You have permission");
                return true;
            } else {

                Log.e("Permission error","You have asked for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //you dont need to worry about these stuff below api level 23
            Log.e("Permission error","You already have the permission");
            return true;
        }
    }

    void saveFile(Uri uri, String bookFile)
    {
        DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                DownloadManager.Request.NETWORK_MOBILE);
        request.allowScanningByMediaScanner();
        request.setTitle(bookFile);
        request.setDescription("Downloading");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, bookFile);
        request.setMimeType("*/*");

        try {
            downloadmanager.enqueue(request);
        }catch(Exception e) {
            Log.e(TAG, "DownloadCurrentBook() << Faild");
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),AllProductsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            //you have the permission now.
            downloadCurrentBook(book.getFile());
        }
    }

}