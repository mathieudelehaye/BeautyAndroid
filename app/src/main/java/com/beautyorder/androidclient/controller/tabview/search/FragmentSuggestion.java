//
//  FragmentSuggestion.java
//
//  Created by Mathieu Delehaye on 30/03/2023.
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

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.NonNull;
import com.beautyorder.androidclient.Helpers;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.databinding.FragmentSuggestionBinding;

public class FragmentSuggestion extends FragmentWithSearch {
    private FragmentSuggestionBinding mBinding;
    private Context mContext;
    private View mContainerView;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        mBinding = FragmentSuggestionBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.v("BeautyAndroid", "Suggestion view created at timestamp: "
                + Helpers.getTimestamp());

        super.onViewCreated(view, savedInstanceState);

        mContext = getContext();
        mContainerView = view;
    }

    public void setListAdapter(BaseAdapter adapter) {
        final var suggestionsList = (ListView) getView().findViewById(R.id.suggestion_list);
        if(suggestionsList == null) {
            Log.e("BeautyAndroid", "Cannot set the adapter, as no suggestions list view");
        }

        suggestionsList.setAdapter(adapter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            Log.d("BeautyAndroid", "Suggestions page becomes visible");

            if (mContext == null) {
                Log.w("BeautyAndroid", "Cannot prepare the edit text view, as no context");
                return;
            }

            // When the view is displayed, the keyboard is visible. So, give the focus to the edit text view
            final var searchView = (EditText) mContainerView.findViewById(R.id.search_view_query);
            if (searchView == null) {
                return;
            }

            Log.v("BeautyAndroid", "Focus requested on the edit text view");
            searchView.requestFocus();

            // Show the keyboard
            final var inputManager = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

            // Clear the edit text
            searchView.getText().clear();
        }
    }

    @Override
    protected void searchAndDisplayItems() {
    }
}
