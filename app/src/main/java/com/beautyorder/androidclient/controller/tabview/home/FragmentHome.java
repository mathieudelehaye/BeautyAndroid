//
//  FragmentHome.java
//
//  Created by Mathieu Delehaye on 25/03/2023.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
//
//  Copyright © 2023 Mathieu Delehaye. All rights reserved.
//
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//  FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

package com.beautyorder.androidclient.controller.tabview.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.controller.tabview.FragmentWithSearch;
import com.beautyorder.androidclient.controller.tabview.TabViewActivity;
import com.beautyorder.androidclient.databinding.FragmentHomeBinding;
import com.beautyorder.androidclient.model.AppUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FragmentHome extends FragmentWithSearch {
    private FragmentHomeBinding mBinding;
    protected FirebaseFirestore mDatabase;
    protected SharedPreferences mSharedPref;
    protected Context mCtx;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseFirestore.getInstance();

        mCtx = view.getContext();

        mSharedPref = mCtx.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        updateUserScore();

        // Hide the back arrow button from the search box
        ViewGroup searchBackLayout = view.findViewById(R.id.search_box_back_layout);
        if (searchBackLayout == null) {
            Log.e("BeautyAndroid", "No view found when hiding the search back button");
            return;
        }
        searchBackLayout.setVisibility(View.GONE);
    }

    protected void displayScoreBox(String fragmentName, int layout_id) {
        // Show or hide the score box according to the locale
        if (!mustShowBrand()) {
            var fragmentRootView = getView();
            if (fragmentRootView == null) {
                Log.w("BeautyAndroid", "Cannot display or hide the score box in the " + fragmentName
                    + " fragment, as no fragment root view");
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
        if (mCtx == null) {
            Log.w("BeautyAndroid", "Cannot check if brand must be shown, as no context");
            return false;
        }

        return !mCtx.getResources().getConfiguration().getLocales().get(0).getDisplayName().contains("Belgique");
    }

    @Override
    protected void searchAndDisplayItems() {
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

                var mainActivity = (TabViewActivity) getActivity();
                if (mainActivity == null) {
                    Log.w("BeautyAndroid", "Cannot update the score, as no main activity found");
                    return;
                }
                mainActivity.showScore(userScore);
            });
    }
}
