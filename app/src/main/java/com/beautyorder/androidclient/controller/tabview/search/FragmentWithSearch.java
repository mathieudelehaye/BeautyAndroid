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
import android.view.View;
import android.widget.SearchView;
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
    protected Context mCtx;
    protected abstract void searchAndDisplayItems();

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseFirestore.getInstance();

        mCtx = view.getContext();

        mSharedPref = mCtx.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        setupSearchBox();
    }

    protected void setupSearchBox() {
        final Activity activity = getActivity();
        if (activity == null || mCtx == null) {
            Log.e("BeautyAndroid", "Cannot set up the search box, as no activity or no context");
            return;
        }

        // Get the SearchView and set the searchable configuration
        var searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        var searchView = (SearchView) getView().findViewById(R.id.search_box);
        SearchableInfo configuration = searchManager.getSearchableInfo(activity.getComponentName());
        final String queryHint = getString(configuration.getHintId());
        searchView.setQueryHint(queryHint);

        // Overwrite the suggestion adapter, so the drop-down menu is replaced by a dedicated screen
        var suggestionsAdapter = new SuggestionsAdapter(mCtx, searchView, configuration);
        suggestionsAdapter.setDropDownViewTheme(null);  // Prevent the drop-down view from showing
        searchView.setSuggestionsAdapter(suggestionsAdapter);

        // Do not iconify the widget; expand it by default
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.v("BeautyAndroid", "Query text submitted: " + s);

                // Store the search query as a class static property
                FragmentResultList.setResultQuery(s);

                Helpers.callObjectMethod(activity, TabViewActivity.class, "showResult",
                    new FragmentResultList(), null, null);

                // Return true in order to override the standard behavior and not to
                // send the `android.intent.action.SEARCH` intent to any searchable
                // activity.
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                return false;
            }
        });

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            Helpers.callObjectMethod(getActivity(), TabViewActivity.class, "navigate",
                TabViewActivity.FragmentType.SUGGESTION, null, null);
        });
    }
}
