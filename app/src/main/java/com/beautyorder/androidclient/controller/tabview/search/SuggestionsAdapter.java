//
//  SuggestionsAdapter.java
//
//  Created by Mathieu Delehaye on 2/04/2023.
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

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;
import com.beautyorder.androidclient.R;


public class SuggestionsAdapter extends CursorAdapter {

    private static final int QUERY_LIMIT = 50;
    private Context mContext;
    private final SearchManager mSearchManager;
    private final EditText mSearchView;
    private final SearchableInfo mSearchable;
    private Cursor mLastFoundSuggestions;

    public SuggestionsAdapter(Context context, SearchView search, SearchableInfo searchable) {
        super(context, null /* no initial cursor */, true /* auto-requery */);

        mContext = context;
        mSearchManager = (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        mSearchable = searchable;

        mSearchView = search.findViewById(R.id.search_view_query);
        if (mSearchView == null) {
            Log.e("BeautyAndroid", "Error with suggestions adapter, as no Query edit text");
        }
    }

    /**
     * Use the search suggestions provider to obtain a live cursor.  This will be called
     * in a worker thread, so it's OK if the query is slow (e.g. round trip for suggestions).
     * The results will be processed in the UI thread and changeCursor() will be called.
     * changeCursor() doesn't get called if cursor is null.
     */
    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        Log.d("BeautyAndroid", "runQueryOnBackgroundThread(" + constraint + ")");

        final String query = (constraint == null) ? "" : constraint.toString();

        if (mSearchView.getVisibility() != View.VISIBLE
            || mSearchView.getWindowVisibility() != View.VISIBLE) {

            return null;
        }

        try {
            final Cursor cursor = getSuggestions(mSearchable, query, QUERY_LIMIT);
            if (cursor != null) {
                // TODO: do not use a property here, but the method return value instead.
                mLastFoundSuggestions = cursor;
                return cursor;
            }
        } catch (RuntimeException e) {
            Log.w("BeautyAndroid", "Search suggestions query threw an exception: ", e);
        }

        return null;
    }

    /* From android/app/SearchManager.java. Though public, it cannot be called, as this is not a
       part of the SDK (@UnsupportedAppUsage). */
    public Cursor getSuggestions(SearchableInfo searchable, String query, int limit) {
        if (searchable == null) {
            return null;
        }

        String authority = searchable.getSuggestAuthority();
        if (authority == null) {
            return null;
        }

        Uri.Builder uriBuilder = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(authority)
            .query("")  // TODO: Remove, workaround for a bug in Uri.writeToParcel()
            .fragment("");  // TODO: Remove, workaround for a bug in Uri.writeToParcel()

        // if content path provided, insert it now
        final String contentPath = searchable.getSuggestPath();
        if (contentPath != null) {
            uriBuilder.appendEncodedPath(contentPath);
        }

        // append standard suggestion query path
        uriBuilder.appendPath(SearchManager.SUGGEST_URI_PATH_QUERY);

        // get the query selection, may be null
        String selection = searchable.getSuggestSelection();
        // inject query, either as selection args or inline
        String[] selArgs = null;
        if (selection != null) {    // use selection if provided
            selArgs = new String[] { query };
        } else {                    // no selection, use REST pattern
            uriBuilder.appendPath(query);
        }

        if (limit > 0) {
            uriBuilder.appendQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT, String.valueOf(limit));
        }

        Uri uri = uriBuilder.build();

        // finally, make the query
        return mContext.getContentResolver().query(uri, null, selection, selArgs, null);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
    }

    @Override
    public int getCount() {
        if (mLastFoundSuggestions == null) {
            return 0;
        }

        final int count = mLastFoundSuggestions.getCount();

        return count;
    }

    @Override
    public Object getItem(int position) {
        if (mLastFoundSuggestions == null) {
            return null;
        }

        mLastFoundSuggestions.moveToPosition(position);
        return mLastFoundSuggestions;
    }

    @Override
    public long getItemId(int position) {
        if (mLastFoundSuggestions == null) {
            return -1;
        }

        mLastFoundSuggestions.moveToPosition(position);
        final String id = mLastFoundSuggestions.getString(0);

        return Long.parseLong(id);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mLastFoundSuggestions == null) {
            return null;
        }

        mLastFoundSuggestions.moveToPosition(position);
        final String value = mLastFoundSuggestions.getString(1);

        View view = View.inflate(mContext, R.layout.suggestion_list_item,null);
        TextView textView = view.findViewById(R.id.suggestion_list_item_text);
        textView.setText(value);

        return view;
    }
}
