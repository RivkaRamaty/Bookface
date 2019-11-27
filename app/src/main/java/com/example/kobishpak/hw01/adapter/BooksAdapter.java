package com.example.kobishpak.hw01.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kobishpak.hw01.BookDetailsActivity;
import com.example.kobishpak.hw01.R;
import com.example.kobishpak.hw01.model.Book;
import com.example.kobishpak.hw01.model.User;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Iterator;
import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {

    private final String TAG = "BooksAdapter";
    private List<BookWithKey> booksList;
    private User user;
    private int m_Discount = 0;
    private FirebaseAuth m_FirebaseAuth = FirebaseAuth.getInstance();

    public BooksAdapter(List<BookWithKey> booksList, User user, int i_Discount) {
        this.m_Discount = i_Discount;
        this.booksList = booksList;
        this.user = user;
    }

    public BooksAdapter(List<BookWithKey> booksList, User user) {
        this.booksList = booksList;
        this.user = user;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.e(TAG,"onCreateViewHolder() >>");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);

        Log.e(TAG,"onCreateViewHolder() <<");
        return new BookViewHolder(parent.getContext(),itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final BookViewHolder holder, int position) {

        Log.e(TAG,"onBindViewHolder() >> " + position);

        Book book = booksList.get(position).getBook();
        String bookKey = booksList.get(position).getKey();

        StorageReference thumbRef = FirebaseStorage
                .getInstance()
                .getReference()
                .child("Thumbs/" + book.getThumbImage());

        thumbRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                // Load the image using Glide
                Glide.with(holder.getContext())
                .load(uri)
                .into(holder.getThumbImage());
                Log.e(TAG, "DownloadCurrentBook() << SUCCESS");
            }
        });

        holder.setSelectedBook(book);
        holder.setSelectedBookKey(bookKey);
        holder.getName().setText(book.getName());
        holder.setBookFile(book.getFile());
        holder.getGenre().setText(book.getGenre());
        holder.getArtist().setText(book.getArtist());

        if (book.getReviewsCount() >0) {
            holder.getReviewsCount().setText("("+book.getReviewsCount()+")");
            holder.getRating().setRating((float)(book.getRating() / book.getReviewsCount()));
        }
        //Check if the user already purchased the book if set the text to Play
        //If not to BUY $X
        int price = book.getPrice() - m_Discount;
        holder.getPrice().setText("$"+price);

        Iterator i = user.getMyBooks().iterator();
        while (i.hasNext()) {
            if (i.equals(bookKey)) {
                holder.getPrice().setTextColor(ContextCompat.getColor(holder.getContext(),R.color.colorPrimary));
                break;
            }
            i.next();
        }

        Log.e(TAG,"onBindViewHolder() << "+ position);
    }


    @Override
    public int getItemCount() {
        return booksList.size();
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {

        private CardView bookCardView;
        private ImageView thumbImage;
        private TextView name;
        private TextView artist;
        private TextView genre;
        private TextView price;
        private TextView reviewsCount;
        private String bookFile;
        private String thumbFile;
        private Context context;
        private RatingBar rating;
        private Book selectedBook;
        private String selectedBookKey;

        public BookViewHolder(Context context, View view) {

            super(view);

            bookCardView = view.findViewById(R.id.card_view_book);
            thumbImage =  view.findViewById(R.id.book_thumb_image);
            name = view.findViewById(R.id.book_name);
            artist =  view.findViewById(R.id.book_reviewer_mail);
            genre =  view.findViewById(R.id.book_genre);
            price =  view.findViewById(R.id.book_price);
            reviewsCount =  view.findViewById(R.id.book_review_count);
            rating =  view.findViewById(R.id.book_rating);

            this.context = context;

            bookCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e(TAG, "CardView.onClick() >> name=" + selectedBook.getName());

                    Context context = view.getContext();
                    Intent intent = new Intent(context, BookDetailsActivity.class);
                    intent.putExtra("book", selectedBook);
                    intent.putExtra("key", selectedBookKey);
                    intent.putExtra("user",user);
                    intent.putExtra("discount", m_Discount);
                    context.startActivity(intent);
                }
            });
        }

        public TextView getPrice() {
            return price;
        }

        public TextView getName() {
            return name;
        }

        public ImageView getThumbImage() {
            return thumbImage;
        }

        public void setBookFile(String file) {
            this.bookFile = file;
        }

        public TextView getArtist() {
            return artist;
        }

        public TextView getGenre() {
            return genre;
        }

        public void setThumbFile(String thumbFile) {
            this.thumbFile = thumbFile;
        }

        public Context getContext() {
            return context;
        }

        public RatingBar getRating() {
            return rating;
        }

        public void setSelectedBook(Book selectedBook) {
            this.selectedBook = selectedBook;
        }

        public void setSelectedBookKey(String selectedBookKey) {
            this.selectedBookKey = selectedBookKey;
        }

        public TextView getReviewsCount() {return reviewsCount;}
    }
}
