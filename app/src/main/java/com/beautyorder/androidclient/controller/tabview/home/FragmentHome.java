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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import com.android.java.androidjavatools.Helpers;
import com.android.java.androidjavatools.model.AppUser;
import com.android.java.androidjavatools.model.ResultItemInfo;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.controller.tabview.search.FragmentWithSearch;
import com.beautyorder.androidclient.controller.tabview.TabViewActivity;
import com.beautyorder.androidclient.databinding.FragmentHomeBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FragmentHome extends FragmentWithSearch {
    private FragmentHomeBinding mBinding;
    protected Context mContext;
    private View mFragmentRootView;
    protected FirebaseFirestore mDatabase;
    protected SharedPreferences mSharedPref;

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

        mContext = getContext();

        mSharedPref = mContext.getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        mFragmentRootView = getView();

        updateRecentRP();
        updateRecentSearches();

        updateUserScore();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.d("BeautyAndroid", "Home view becomes visible");

            updateRecentRP();
            updateRecentSearches();
        }
    }

    public void updateRecentRP() {
        final int buttonNumber = 2;

        if (mContext == null) {
            Log.w("BeautyAndroid", "Cannot update the recent searches, as no context");
            return;
        }

        if (mFragmentRootView == null) {
            Log.w("BeautyAndroid", "Cannot update the recent searches, as no root view");
            return;
        }

        final var recyclePoints = (Integer)Helpers.callObjectMethod(mContext, TabViewActivity.class,
            "getPreviousRPNumber", null, null, null);

        if (recyclePoints == null) {
            return;
        }

        for(int i = 0; i < buttonNumber; i++) {
            var historyButton =
                (i == 0) ? (Button)mFragmentRootView.findViewById(R.id.rp_history_button_1a) :
                (Button)mFragmentRootView.findViewById(R.id.rp_history_button_1b);

            if (i < recyclePoints) {
                // Update the RP history buttons
                final var recyclingPoint = (ResultItemInfo)Helpers.callObjectMethod(mContext, TabViewActivity.class,
                    "getPreviousRP", i, null, null);
                if (recyclingPoint == null) {
                    continue;
                }

                final String key = recyclingPoint.getTitle();
                final String title = recyclingPoint.getTitle();

                Log.v("BeautyAndroid", "updateRecentRP: age = " + i + ", key = " + key
                    + ", title = " + title);

                historyButton.setText(title.substring(0, Math.min(key.length(), 15)));

                historyButton.setOnClickListener(v -> showResult(recyclingPoint));
                historyButton.setVisibility(View.VISIBLE);
            } else {
                // Hide the button if no related RP
                historyButton.setVisibility(View.GONE);
            }
        }
    }

    public void updateRecentSearches() {
        final int buttonNumber = 4;

        if (mContext == null) {
            Log.w("BeautyAndroid", "Cannot update the recent searches, as no context");
            return;
        }

        if (mFragmentRootView == null) {
            Log.w("BeautyAndroid", "Cannot update the recent searches, as no root view");
            return;
        }

        final var queries = (Integer)Helpers.callObjectMethod(mContext, TabViewActivity.class,
            "getPreviousQueryNumber", null, null, null);

        if (queries == null) {
            return;
        }

        for(int i = 0; i < buttonNumber; i++) {
            var historyButton =
                (i == 0) ? (Button)mFragmentRootView.findViewById(R.id.search_history_button_1a) :
                (i == 1) ? (Button)mFragmentRootView.findViewById(R.id.search_history_button_1b) :
                (i == 2) ? (Button)mFragmentRootView.findViewById(R.id.search_history_button_2a) :
                (Button)mFragmentRootView.findViewById(R.id.search_history_button_2b);

            if (i < queries) {
                // Update the search history buttons
                final String query = (String)Helpers.callObjectMethod(mContext, TabViewActivity.class,
                    "getPreviousSearchQuery", i, null, null);
                if (query == null) {
                    continue;
                }

                Log.v("BeautyAndroid", "updateRecentSearches: age = " + i + ", query = " + query);

                historyButton.setText(query.substring(0, Math.min(query.length(), 15)));
                historyButton.setOnClickListener(v -> runSearch(query));
                historyButton.setVisibility(View.VISIBLE);
            } else {
                // Hide the button if no related query
                historyButton.setVisibility(View.GONE);
            }
        }
    }

    protected void displayScoreBox(String fragmentName, int layout_id) {
        if (mFragmentRootView == null) {
            Log.w("BeautyAndroid", "Cannot display or hide the score box in the " + fragmentName
                + " fragment, as no fragment root view");
            return;
        }

        // Show or hide the score box according to the locale
        if (!mustShowBrand()) {

            View scoreLayout = mFragmentRootView.findViewById(layout_id);
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
