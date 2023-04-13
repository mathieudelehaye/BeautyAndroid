//
//  FragmentWithSearch.java
//
//  Created by Mathieu Delehaye on 22/01/2023.
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
//  You should have received a copy of the GNU Affero General Public License along with this program. If not,
//  see <https://www.gnu.org/licenses/>.

package com.beautyorder.androidclient.controller.tabview.search;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.beautyorder.androidclient.Helpers;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.controller.tabview.TabViewActivity;
import com.beautyorder.androidclient.controller.tabview.result.list.FragmentResultList;
import com.google.firebase.firestore.FirebaseFirestore;

public abstract class FragmentWithSearch extends Fragment {
    protected FirebaseFirestore mDatabase;
    protected SharedPreferences mSharedPref;
    protected Context mContext;
    protected SearchableInfo mConfiguration;
    protected SearchView mSearchView;
    protected EditText mSearchQuery;
    protected abstract void searchAndDisplayItems();

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseFirestore.getInstance();

        mContext = view.getContext();

        mSharedPref = mContext.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        setupSearchBox(view);
    }

    protected void runSearch(String query) {
        FragmentResultList.setResultQuery(query);

        Helpers.callObjectMethod(mContext, TabViewActivity.class, "showResult",
            new FragmentResultList(), null, null);
    }

    private void setupSearchBox(@NonNull View view) {
        final Activity activity = getActivity();
        if (activity == null || mContext == null) {
            Log.e("BeautyAndroid", "Cannot set up the search box, as no activity or no context");
            return;
        }

        mSearchView = view.findViewById(R.id.search_box_search_view);

        final boolean isSuggestionFragment = this instanceof FragmentSuggestion;
        final boolean isResultListFragment = this instanceof FragmentResultList;

        mSearchQuery = mSearchView.findViewById(R.id.search_view_query);
        if (mSearchQuery == null) {
            Log.e("BeautyAndroid", "Error with fragment with search, as no query edit text");
            return;
        }

        mSearchQuery.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                return;
            }
            Log.v("BeautyAndroid", "View " + v + " has focus");

            if (isSuggestionFragment) {
                return;
            }

            // Only if Suggestions fragment not displayed

            Helpers.callObjectMethod(activity, TabViewActivity.class, "navigate",
                TabViewActivity.FragmentType.SUGGESTION, null, null);
        });

        mSearchQuery.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
            {
                final String query = mSearchQuery.getText().toString();
                Log.v("BeautyAndroid", "Search query validated by pressing enter: " + query);

                // Start the search
                runSearch(query);

                return false;
            }

            return false;
        });

        // Set the searchable configuration
        final var searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        mConfiguration = searchManager.getSearchableInfo(activity.getComponentName());
        mSearchView.setSearchableInfo(mConfiguration);

        if (!isSuggestionFragment && !isResultListFragment) {
            return;
        }

        // Only if Suggestions or Result list fragment displayed

        // Show the Back button from the search box
        ViewGroup searchBackLayout = mSearchView.findViewById(R.id.search_view_back_button_layout);
        if (searchBackLayout == null) {
            Log.e("BeautyAndroid", "No view found when showing the search Back button");
            return;
        }
        searchBackLayout.setVisibility(View.VISIBLE);
    }
}
