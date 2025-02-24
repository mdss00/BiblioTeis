package com.example.biblioteisandroid;

import com.example.biblioteisandroid.API.models.User;

public class SearchHolder {
    private static SearchHolder instance;
    private User user;

    private SearchHolder() {
        user = new User();
    }

    public static SearchHolder getInstance() {
        if (instance == null) {
            instance = new SearchHolder();
        }
        return instance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User usr) {
        user = usr;
    }
}
