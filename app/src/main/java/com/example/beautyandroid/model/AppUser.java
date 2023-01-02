//
//  AppUser.java
//
//  Created by Mathieu Delehaye on 24/12/2022.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
//
//  Copyright © 2022 Mathieu Delehaye. All rights reserved.
//
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//  FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

package com.example.beautyandroid.model;

public class AppUser {
    public enum AuthenticationType {
        NONE,
        NOT_REGISTERED,
        REGISTERED
    }

    private static final AppUser mInstance = new AppUser();

    private AuthenticationType authenticationType = AuthenticationType.NONE;

    private StringBuilder id = new StringBuilder("");

    // private constructor to avoid client applications using it
    private AppUser(){}

    public static AppUser getInstance() {
        return mInstance;
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
