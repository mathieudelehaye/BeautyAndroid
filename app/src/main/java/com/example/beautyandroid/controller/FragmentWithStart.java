//
//  FragmentWithStart.java
//
//  Created by Mathieu Delehaye on 2/01/2022.
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

package com.example.beautyandroid.controller;

import android.content.SharedPreferences;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.beautyorder.androidclient.R;
import com.example.beautyandroid.model.AppUser;

public class FragmentWithStart extends Fragment {

    public String getAnonymousUidFromPreferences(SharedPreferences _preferences) {
        StringBuilder anonymousUid = new StringBuilder();
        anonymousUid.append(_preferences.getString(getString(R.string.anonymous_uid), ""));

        if (!anonymousUid.toString().equals("")) {
            // Reuse the anonymous uid if it already exists in the app preferences
            return anonymousUid.toString();
        } else {
            return "";
        }
    }

    public void startAppWithUser(SharedPreferences _preferences, int destination,
        String _uid, AppUser.AuthenticationType _userType) {

        // Store the uid in the app preferences
        _preferences.edit().putString(getString(R.string.app_uid), _uid)
            .commit();
        Log.v("BeautyAndroid", "Latest uid stored to the app preferences: " + _uid);

        // Update the current app user
        AppUser.getInstance().authenticate(_uid, _userType);

        NavHostFragment.findNavController(this)
            .navigate(destination);
    }
}
