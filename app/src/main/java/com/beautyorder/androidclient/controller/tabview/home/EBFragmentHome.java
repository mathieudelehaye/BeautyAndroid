//
//  EBFragmentHome.java
//
//  Created by Mathieu Delehaye on 20/04/2023.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
//
//  Copyright © 2023 Mathieu Delehaye. All rights reserved.
//
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
//  Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
//  warranty of MERCHANTABILITY or FITNESS
//  FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this program. If not, see
//  <https://www.gnu.org/licenses/>.

package com.beautyorder.androidclient.controller.tabview.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import com.android.java.androidjavatools.controller.tabview.home.FragmentHome;
import com.android.java.androidjavatools.controller.template.ResultProvider;
import com.android.java.androidjavatools.model.AppUser;
import com.beautyorder.androidclient.controller.tabview.EBTabViewActivity;
import com.beautyorder.androidclient.R;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class EBFragmentHome extends FragmentHome {
    protected SharedPreferences mSharedPref;

    public EBFragmentHome(ResultProvider provider) {
        super(provider);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSharedPref = mContext.getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        updateUserScore();
    }

    private void updateUserScore() {

        if (mDatabase == null) {
            return;
        }

        // Display the user score
        mDatabase.collection("userInfos")
            .whereEqualTo("__name__", AppUser.getInstance().getId())
            .get()
            .addOnCompleteListener(task -> {
                // Display score
                int userScore = 0;

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        var scoreData = document.getData().get("score").toString();
                        userScore = (!scoreData.equals("")) ? Integer.parseInt(scoreData) : 0;
                    }
                } else {
                    Log.d("BeautyAndroid", "Error getting documents: ", task.getException());
                }

                Log.d("BeautyAndroid", "userScore = " + userScore);

                var mainActivity = (EBTabViewActivity) getActivity();
                if (mainActivity == null) {
                    Log.w("BeautyAndroid", "Cannot update the score, as no main activity found");
                    return;
                }
                mainActivity.showScore(userScore);
            });
    }

    protected void displayScoreBox(String fragmentName, int layout_id) {
        // Show or hide the score box according to the locale
        if (!mustShowBrand()) {

            View fragmentRootView = getView();
            if (fragmentRootView == null) {
                return;
            }

            View scoreLayout = fragmentRootView.findViewById(layout_id);
            if (scoreLayout == null) {
                Log.w("BeautyAndroid", "Cannot display or hide the score box in the " + fragmentName
                    + " fragment, as no score layout");
                return;
            }

            Log.v("BeautyAndroid", "The score box is hidden in the " + fragmentName
                    + " fragment");
            scoreLayout.setVisibility(View.GONE);
        } else {
            Log.v("BeautyAndroid", "The score box is shown in the " + fragmentName
                    + " fragment");
        }
    }

    protected boolean mustShowBrand() {
        if (mContext == null) {
            Log.w("BeautyAndroid", "Cannot check if brand must be shown, as no context");
            return false;
        }

        return !mContext.getResources().getConfiguration().getLocales().get(0).getDisplayName().contains("Belgique");
    }

    @Override
    protected void searchAndDisplayItems() {
    }
}
