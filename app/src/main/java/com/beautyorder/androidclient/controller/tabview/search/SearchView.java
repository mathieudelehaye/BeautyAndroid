//  SearchView.java
//
//  Created by Mathieu Delehaye on 9/04/2023.
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


package com.beautyorder.androidclient.controller.tabview.search;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.database.DataSetObserver;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.widget.LinearLayoutCompat;
import com.beautyorder.androidclient.controller.tabview.TabViewActivity;
import com.beautyorder.androidclient.R;

public class SearchView extends LinearLayoutCompat implements Filter.FilterListener {
    private Activity mActivity;
    private Context mContext;
    private View mContainerView;
    private TabViewActivity.FragmentType mShownFragment = TabViewActivity.FragmentType.NONE;
    private EditText mQuery;
    private DataSetObserver mObserver;
    private CursorAdapter mAdapter;
    private Filter mFilter;

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;

        final var inflater = (LayoutInflater) context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);
        mContainerView = inflater.inflate(R.layout.search_view, this, true);

        init();
    }

    public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
        if (mObserver == null) {
            mObserver = new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                }

                @Override
                public void onInvalidated() {
                    super.onInvalidated();
                }
            };
        } else if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mObserver);
        }

        mAdapter = (CursorAdapter) adapter;

        if (mAdapter != null) {
            //noinspection unchecked
            mFilter = ((Filterable) mAdapter).getFilter();
            adapter.registerDataSetObserver(mObserver);
        } else {
            mFilter = null;
        }
    }

    @Override
    public void onFilterComplete(int count) {
    }

    private void init() {
        if (mContext == null) {
            Log.e("BeautyAndroid", "Error with search view layout, as no context");
            return;
        }

        mActivity = (Activity)mContext;
        if (mActivity == null) {
            Log.e("BeautyAndroid", "Error with search view layout, as no activity");
            return;
        }

        mQuery = mContainerView.findViewById(R.id.search_view_query);
        if (mQuery == null) {
            Log.e("BeautyAndroid", "Error with search view layout, as no query edit text");
            return;
        }

        mQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mQuery.getText().toString().equals("")) {
                    // If the query is empty, hide the Clear button
                } else {
                }

                performFiltering();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Set the searchable configuration
        final var searchManager = (SearchManager) mActivity.getSystemService(Context.SEARCH_SERVICE);
        final var configuration = searchManager.getSearchableInfo(mActivity.getComponentName());
        //var suggestionsAdapter = new SuggestionsAdapter(mContext, queryView, configuration);
        //mQuery.setAdapter(suggestionsAdapter);
        final var queryHint = mActivity.getString(configuration.getHintId());
        mQuery.setHint(queryHint);
    }

    private void performFiltering() {
        if (mFilter == null) {
            return;
        }

        mFilter.filter(mQuery.getText(), this);
    }
}
