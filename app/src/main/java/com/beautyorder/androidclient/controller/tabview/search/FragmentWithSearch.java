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
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.beautyorder.androidclient.Helpers;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.controller.tabview.TabViewActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public abstract class FragmentWithSearch extends Fragment {
    protected FirebaseFirestore mDatabase;
    protected SharedPreferences mSharedPref;
    protected Context mCtx;
    protected abstract void searchAndDisplayItems();

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseFirestore.getInstance();

        mCtx = view.getContext();

        mSharedPref = mCtx.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        setupSearchBox(view);
    }

    private void setupSearchBox(@NonNull View view) {
        final Activity activity = getActivity();
        if (activity == null || mCtx == null) {
            Log.e("BeautyAndroid", "Cannot set up the search box, as no activity or no context");
            return;
        }

        final SearchView searchView = view.findViewById(R.id.search_box_search_view);

        final boolean isSuggestionFragment = this instanceof FragmentSuggestion;

        final var query = (EditText)searchView.findViewById(R.id.search_view_query);
        if (query == null) {
            Log.e("BeautyAndroid", "Error with fragment with search, as no query edit text");
            return;
        }

        query.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                return;
            }
            Log.v("BeautyAndroid", "View " + v + " has focus");

            if (isSuggestionFragment) {
                return;
            }

            // Only if Suggestions fragment not displayed

            // Hide the toolbar
            Helpers.callObjectMethod(activity, TabViewActivity.class, "toggleToolbar",
                false, null, null);

            Helpers.callObjectMethod(activity, TabViewActivity.class, "navigate",
                TabViewActivity.FragmentType.SUGGESTION, null, null);
        });

        if (!isSuggestionFragment) {
            return;
        }

        // Only if Suggestions fragment displayed

        // Show the Back button from the search box
        ViewGroup searchBackLayout = searchView.findViewById(R.id.search_view_back_button_layout);
        if (searchBackLayout == null) {
            Log.e("BeautyAndroid", "No view found when showing the search Back button");
            return;
        }
        searchBackLayout.setVisibility(View.VISIBLE);

        // Implement the back button behaviour
        Button searchBackButton = searchView.findViewById(R.id.search_view_back_button);
        if (searchBackButton == null) {
            Log.e("BeautyAndroid", "No view found when implementing the behaviour for the search "
                + "Back button");
            return;
        }
        searchBackButton.setOnClickListener(v -> {
            // Show the toolbar
            Helpers.callObjectMethod(activity, TabViewActivity.class, "toggleToolbar",
                true, null, null);

            // Hide the keyboard
            final var inputManager = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

            Helpers.callObjectMethod(activity, TabViewActivity.class, "navigateBack",
                null, null, null);
        });
    }
}
