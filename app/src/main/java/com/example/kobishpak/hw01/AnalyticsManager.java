package com.example.kobishpak.hw01;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

//import com.appsee.Appsee;
import com.example.kobishpak.hw01.model.Book;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsManager {
    private static String TAG = "AnalyticsManager";
    private static AnalyticsManager mInstance = null;
    private FirebaseAnalytics mFirebaseAnalytics;
    private MixpanelAPI mMixpanel;
    public static final String MIXPANEL_TOKEN = "cd714d2e032af3efeacddd5baa7dfee2";

    private AnalyticsManager() {}

    public static AnalyticsManager getInstance() {
        if (mInstance == null) {
            mInstance = new AnalyticsManager();
        }
        return (mInstance);
    }

    public void init(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        mMixpanel = MixpanelAPI.getInstance(context,MIXPANEL_TOKEN);
    }

    public void trackSearchEvent(String searchString) {
        String eventName = "search";

        //Firebase
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.SEARCH_TERM, searchString);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH,params);

        //MixPanel
        Map<String, Object> eventParams2 = new HashMap<>();
        eventParams2.put("search term", searchString);
        mMixpanel.trackMap(eventName,eventParams2);
    }

    public void trackSignupEvent(String signupMethod) {
        String eventName = "signup";
        Bundle params = new Bundle();
        params.putString("signupMethod", signupMethod);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP,params);

        //Mixpanel
        Map<String, Object> eventParams2 = new HashMap<>();
        eventParams2.put("signup method", signupMethod);
        mMixpanel.trackMap(eventName,eventParams2);
    }


    public void trackLoginEvent(String loginMethod) {

        String eventName = "login";
        Bundle params = new Bundle();
        params.putString("login_method", loginMethod);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN,params);

        //Mixpanel
        Map<String, Object> eventParams2 = new HashMap<>();
        eventParams2.put("signup method", loginMethod);
        mMixpanel.trackMap(eventName,eventParams2);
    }

    public void trackBookAcquiredEvent(String event , Book book) {
        Bundle params = new Bundle();

        params.putString("book_genre", book.getGenre());
        params.putString("book_name", book.getName());
        params.putString("book_author", book.getArtist());
        params.putDouble("book_price",book.getPrice());
        params.putDouble("book_rating",book.getRating());

        mFirebaseAnalytics.logEvent(event,params);

        //Mixpanel
        Map<String, Object> eventParams2 = new HashMap<>();
        eventParams2.put("book_genre", book.getGenre());
        eventParams2.put("book_name", book.getName());
        eventParams2.put("book_author", book.getArtist());
        eventParams2.put("book_price",String.valueOf(book.getPrice()));
        eventParams2.put("book_rating",String.valueOf(book.getRating()));
        mMixpanel.trackMap(event, eventParams2);
    }

    public void trackBookEvent(String event , Book book) {
        Bundle params = new Bundle();

        params.putString("book_genre", book.getGenre());
        params.putString("book_name", book.getName());
        params.putString("book_author", book.getArtist());
        params.putDouble("book_price",book.getPrice());
        params.putDouble("book_rating",book.getRating());

        mFirebaseAnalytics.logEvent(event,params);

        //Mixpanel
        Map<String, Object> eventParams2 = new HashMap<>();
        eventParams2.put("book_genre", book.getGenre());
        eventParams2.put("book_name", book.getName());
        eventParams2.put("book_author", book.getArtist());
        eventParams2.put("book_price",String.valueOf(book.getPrice()));
        eventParams2.put("book_rating",String.valueOf(book.getRating()));
        mMixpanel.trackMap(event, eventParams2);
    }

    public void trackPurchase(Book book) {
        String eventName = "purchase";
        Bundle params = new Bundle();
        params.putDouble(FirebaseAnalytics.Param.PRICE,book.getPrice());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE,params);

        //Mixpanel
        Map<String, Object> eventParams2 = new HashMap<>();
        eventParams2.put("book_genre", book.getGenre());
        eventParams2.put("book_name", book.getName());
        eventParams2.put("book_author", book.getArtist());
        eventParams2.put("book_price",String.valueOf(book.getPrice()));
        eventParams2.put("book_rating",String.valueOf(book.getRating()));

        mMixpanel.trackMap(eventName,eventParams2);
    }

    public void trackBookRating(Book book ,int userRating) {

        String eventName = "book_rating";
        Bundle params = new Bundle();

        params.putString("book_genre", book.getGenre());
        params.putString("book_name", book.getName());
        params.putString("book_author", book.getArtist());
        params.putDouble("book_price",book.getPrice());
        params.putDouble("book_reviews_count",book.getReviewsCount());
        params.putDouble("book_total_rating",book.getRating());
        params.putDouble("book_user_rating",userRating);

        mFirebaseAnalytics.logEvent(eventName,params);

        //Mixpanel
        Map<String, Object> eventParams2 = new HashMap<>();
        eventParams2.put("book_genre", book.getGenre());
        eventParams2.put("book_name", book.getName());
        eventParams2.put("book_author", book.getArtist());
        eventParams2.put("book_price",String.valueOf(book.getPrice()));
        eventParams2.put("book_reviews_count",String.valueOf(book.getReviewsCount()));
        eventParams2.put("book_total_rating",String.valueOf(book.getRating()));
        eventParams2.put("book_user_rating",String.valueOf(userRating));

        mMixpanel.trackMap(eventName,eventParams2);
    }

    public void setUserID(String id) {
        mFirebaseAnalytics.setUserId(id);

        mMixpanel.identify(id);
        mMixpanel.getPeople().identify(mMixpanel.getDistinctId());
    }

    public void setUserProperty(String name , String value) {
        mFirebaseAnalytics.setUserProperty(name,value);
        mMixpanel.getPeople().set(name,value);
    }

}
