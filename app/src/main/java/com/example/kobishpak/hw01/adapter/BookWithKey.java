package com.example.kobishpak.hw01.adapter;

import com.example.kobishpak.hw01.model.Book;


public class BookWithKey {
    private String key;
    private Book book;

    public BookWithKey(String key, Book book) {
        this.key = key;
        this.book = book;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}
