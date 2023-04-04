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

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import com.beautyorder.androidclient.Helpers;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.databinding.FragmentSuggestionBinding;

public class FragmentSuggestion extends FragmentWithSearch {
    private FragmentSuggestionBinding mBinding;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        mBinding = FragmentSuggestionBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    public void setListAdapter(BaseAdapter adapter) {
        final var suggestionsList = (ListView) getView().findViewById(R.id.suggestion_list);
        if(suggestionsList == null) {
            Log.e("BeautyAndroid", "Cannot set the adapter, as no suggestions list view");
        }

        suggestionsList.setAdapter(adapter);
    }

    @Override
    protected void searchAndDisplayItems() {
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.v("BeautyAndroid", "Suggestion view created at timestamp: "
            + Helpers.getTimestamp());

        super.onViewCreated(view, savedInstanceState);
    }
}
