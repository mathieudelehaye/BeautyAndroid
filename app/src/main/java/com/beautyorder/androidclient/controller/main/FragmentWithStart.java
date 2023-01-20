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

package com.beautyorder.androidclient.controller.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.model.AppUser;

public class FragmentWithStart extends Fragment {

    protected SharedPreferences mSharedPref;

    public FragmentWithStart() {
        super();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Read the app preferences
        mSharedPref = view.getContext().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);
    }

    public String getAnonymousUidFromPreferences() {
        if (mSharedPref == null) {
            Log.w("BeautyAndroid", "Try to get the anonymous uid from the app preferences but "
                + "view not created");
            return "";
        }

        StringBuilder anonymousUid = new StringBuilder();
        anonymousUid.append(mSharedPref.getString(getString(R.string.anonymous_uid), ""));

        if (!anonymousUid.toString().equals("")) {
            String uid = anonymousUid.toString();

            // Reuse the anonymous uid if it already exists in the app preferences
            Log.v("BeautyAndroid", "Anonymous uid loaded from the app preferences: "
                + uid);

            return uid;
        } else {
            return "";
        }
    }

    public void setAnonymousUidToPreferences(String value) {
        if (mSharedPref == null) {
            Log.w("BeautyAndroid", "Try to set the anonymous uid to the app preferences but "
                + "view not created");
            return;
        }

        Log.v("BeautyAndroid", "Anonymous uid stored to the app preferences: "
            + value);

        mSharedPref.edit().putString(getString(R.string.anonymous_uid), value)
            .commit();
    }

    public void startAppWithUser(int _destination, String _uid, AppUser.AuthenticationType _userType) {

        if (mSharedPref == null) {
            Log.w("BeautyAndroid", "Try to start the app with a user but no preference loaded");
            return;
        }

        // Store the uid in the app preferences
        mSharedPref.edit().putString(getString(R.string.app_uid), _uid)
            .commit();
        Log.v("BeautyAndroid", "Latest uid stored to the app preferences: " + _uid);

        // Update the current app user
        AppUser.getInstance().authenticate(_uid, _userType);

        NavHostFragment.findNavController(this)
            .navigate(_destination);
    }
}
