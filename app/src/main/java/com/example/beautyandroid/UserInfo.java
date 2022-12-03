//
//  UserInfo.java
//
//  Created by Mathieu Delehaye on 3/12/2022.
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

package com.beautyorder.androidclient;

public class UserInfo {

    // string variable for
    // storing user first name.
    private String userFirstName;

    // string variable for
    // storing user first name.
    private String userLastName;

    // string variable for storing
    // user address.
    private String userAddress;

    // string variable for
    // storing user city.
    private String userCity;

    // string variable for
    // storing user post code.
    private String userPostCode;

    // an empty constructor is
    // required when using
    // Firebase Realtime Database.
    public UserInfo() {

    }

    // created getter and setter methods
    // for all our variables.
    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserCity() {
        return userCity;
    }

    public void setUserCity(String userCity) {
        this.userCity = userCity;
    }

    public String getPostCode() {
        return userPostCode;
    }

    public void setPostCode(String userPostCode) {
        this.userPostCode = userPostCode;
    }
}
