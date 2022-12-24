package com.example.beautyandroid.model;

import java.io.IOException;

public class AppUser {
    public enum AuthenticationType {
        NONE,
        NOT_REGISTERED,
        REGISTERED
    }

    private static final AppUser instance = new AppUser();

    private AuthenticationType authenticationType = AuthenticationType.NONE;

    private StringBuilder id = new StringBuilder("");

    // private constructor to avoid client applications using it
    private AppUser(){}

    public static AppUser getInstance() {
        return instance;
    }

    public AuthenticationType getAuthenticationType() {
        return this.authenticationType;
    }

    public String getId() {
        return this.id.toString();
    }

    public void authenticate(String _uid, AuthenticationType _type) {
        this.authenticationType = _type;
        this.id.setLength(0);
        this.id.append(_uid);
    }
}
