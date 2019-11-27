package com.example.kobishpak.hw01;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kobishpak.hw01.adapter.BookWithKey;
import com.example.kobishpak.hw01.adapter.BooksAdapter;
import com.example.kobishpak.hw01.model.Book;
import com.example.kobishpak.hw01.model.User;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AllProductsActivity extends AppCompatActivity {

    private static final String TAG = "DisplayBooks";
    private ImageView m_UserImageView;
    private TextView m_UserInfoTextView;
    private EditText m_SearchEditText;
    private Switch mHidePurchasedSwitch;
    private DatabaseReference allBooksRef;
    private DatabaseReference myUserRef;
    private List<BookWithKey> booksList = new ArrayList<>();
    private AnalyticsManager m_AnalyticsManager = AnalyticsManager.getInstance();
    private RecyclerView recyclerView;
    private BooksAdapter booksAdapter;
    private User myUser;
    private FirebaseAuth m_FirebaseAuth;
    private FirebaseUser m_FirebaseUser;
    private boolean doubleBackToExitPressedOnce = false;
    private ProgressDialog progressDialog;
    private static int m_Discount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_products);
        Log.e(TAG, getString(R.string.display_on_create));

        initializeInstances();

        m_SearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                findViewById(R.id.radioButtonByRating).setEnabled(false);
                findViewById(R.id.radioButtonByPrice).setEnabled(false);
                mHidePurchasedSwitch.setEnabled(false);

                showFilteredBooks();
                if (!s.toString().isEmpty()) {
                    m_AnalyticsManager.trackSearchEvent(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0)
                {
                    findViewById(R.id.radioButtonByRating).setEnabled(true);
                    findViewById(R.id.radioButtonByPrice).setEnabled(true);
                    mHidePurchasedSwitch.setEnabled(true);
                }
            }
        });

        m_FirebaseAuth = FirebaseAuth.getInstance();
        m_FirebaseUser = m_FirebaseAuth.getCurrentUser();

        if (m_FirebaseUser == null)
        {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        else {
            myUserRef = FirebaseDatabase.getInstance().getReference("Users/" + m_FirebaseUser.getUid());
            pleaseWait();
            DisplayUserInformation();

            if (m_FirebaseUser.getDisplayName() == null)//m_FirebaseUser.getEmail() == null || m_FirebaseUser.getEmail() == "")
            {
                myUser = new User();
                getAllBooks();
            }
            else {
                FirebaseMessaging.getInstance().subscribeToTopic("all");
                myUserRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Log.e(TAG, "onDataChange(User) >> " + snapshot.getKey());

                        myUser = snapshot.getValue(User.class);

                        getAllBooks();

                        Log.e(TAG, "onDataChange(User) <<");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        Log.e(TAG, "onCancelled(Users) >>" + databaseError.getMessage());
                    }
                });
            }
        }

        mHidePurchasedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                getAllBooks();
            }
        });
    }

    private void showFilteredBooks()
    {
        String searchString = ((EditText)findViewById(R.id.edit_text_search_book)).getText().toString();
        String orderBy = ((RadioButton)findViewById(R.id.radioButtonByRating)).isChecked() ? "rating" : "price";
        Query searchBook;

        Log.e(TAG, "onSearchTextChange() >> searchString="+searchString+ ",orderBy="+orderBy);

        booksList.clear();

        if (!searchString.isEmpty()) {
            searchBook = allBooksRef.orderByChild("name").startAt(searchString).endAt(searchString + "\uf8ff");
        } else {
            searchBook = allBooksRef.orderByChild(orderBy);
        }


        searchBook.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Log.e(TAG, "onDataChange(Query) >> " + snapshot.getKey());

                updateBooksList(snapshot);

                Log.e(TAG, "onDataChange(Query) <<");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.e(TAG, "onCancelled() >>" + databaseError.getMessage());
            }

        });
        Log.e(TAG, "onSearchTextChange() <<");

    }

    private void getAllBooks() {
        booksList.clear();
        booksAdapter = new BooksAdapter(booksList, myUser, m_Discount);
        recyclerView.setAdapter(booksAdapter);

        getAllBooksUsingValueListenrs();
    }

    private void getAllBooksUsingValueListenrs() {

        allBooksRef = FirebaseDatabase.getInstance().getReference("Books");
        Query searchBook = allBooksRef.orderByChild("price");
        searchBook.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Log.e(TAG, "onDataChange(Books) >> " + snapshot.getKey());

                if (mHidePurchasedSwitch.isChecked()) {
                    showPurchesdBooks(snapshot);
                } else {
                    updateBooksList(snapshot);
                }

                Log.e(TAG, "onDataChange(Books) <<");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.e(TAG, "onCancelled(Books) >>" + databaseError.getMessage());
            }
        });
    }

    private void updateBooksList(DataSnapshot snapshot) {

        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
            Book book = dataSnapshot.getValue(Book.class);
            Log.e(TAG, "updateBookList() >> adding book: " + book.getName());
            String key = dataSnapshot.getKey();

            booksList.add(new BookWithKey(key, book));
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void showPurchesdBooks(DataSnapshot snapshot) {

        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
            Book book = dataSnapshot.getValue(Book.class);
            Log.e(TAG, "updateBookList() >> adding book: " + book.getName());
            String key = dataSnapshot.getKey();

            List<String> listOfUserBooks = myUser.getMyBooks();

            for (String list:listOfUserBooks) {

                if (list.equals(key)) {
                    booksList.add(new BookWithKey(key, book));
                }
            }
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public void onRadioButtonClick(View v) {
        switch (v.getId()) {
            case R.id.radioButtonByPrice:
                ((RadioButton)findViewById(R.id.radioButtonByRating)).setChecked(false);
                break;
            case R.id.radioButtonByRating:
                ((RadioButton)findViewById(R.id.radioButtonByPrice)).setChecked(false);
                break;
        }
        showFilteredBooks();
    }


    private void pleaseWait() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading. Please wait...");
        progressDialog.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                progressDialog.dismiss();
            }
        }, 2000); // 2000 milliseconds delay for dialog
    }

    private void initializeInstances() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_exit_to_app_white_24dp);
        m_UserInfoTextView = findViewById(R.id.textViewUserInfo);
        m_UserImageView = findViewById(R.id.userImageView);
        m_SearchEditText = findViewById(R.id.edit_text_search_book);
        recyclerView = findViewById(R.id.books_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mHidePurchasedSwitch = findViewById(R.id.hidePurchasedSwitch);
        m_AnalyticsManager.init(this);
        m_Discount = (new SimpleDateFormat("E")).format(new Date()).equals("Mon") ? 1 : 0; // Bookworm Monday
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onClickLogOutButton();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onClickLogOutButton() {
        m_FirebaseAuth.signOut();
        LoginManager.getInstance().logOut();
        startActivity(new Intent(AllProductsActivity.this, LoginActivity.class));
    }

    private void DisplayUserInformation() {
        if(m_FirebaseUser.getPhotoUrl() != null) {
            Glide.with(AllProductsActivity.this).load(m_FirebaseUser.getPhotoUrl()).into(m_UserImageView);
        }
        else {
            m_UserImageView.setImageResource(R.drawable.anonymous_user_image);
        }

        String userText;
        userText = m_FirebaseUser.getDisplayName() == null ?
        "Hello" :
        String.format("Hello %s", m_FirebaseUser.getDisplayName()) ;

        m_UserInfoTextView.setText(userText);
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
            return;
    }
}
