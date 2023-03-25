//
//  ShowResults.java
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

package com.beautyorder.androidclient.controller.result;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.beautyorder.androidclient.*;
import com.beautyorder.androidclient.controller.Navigator;
import com.beautyorder.androidclient.controller.tabview.*;
import com.beautyorder.androidclient.controller.tabview.dialog.FragmentHelpDialog;
import com.beautyorder.androidclient.controller.result.list.FragmentResultDetail;
import com.beautyorder.androidclient.controller.result.list.FragmentResultList;
import com.beautyorder.androidclient.controller.result.map.FragmentMap;
import com.beautyorder.androidclient.model.ResultItemInfo;
import com.beautyorder.androidclient.model.SearchResult;
import com.google.firebase.firestore.FirebaseFirestore;

public class ShowResultActivity extends AppCompatActivity {
   // Fragments: types
    public enum FragmentType {
        LIST,
        DETAIL,
        MAP,
        NONE
    }

    private FirebaseFirestore mDatabase;
    private SharedPreferences mSharedPref;

    // Fragments: properties
    private Navigator mNavigator = new Navigator(this);
    private FragmentResultList mListFragment = new FragmentResultList();
    private FragmentResultDetail mDetailFragment = new FragmentResultDetail();
    private FragmentMap mMapFragment = new FragmentMap();
    private FragmentType mShownFragmentType = FragmentType.NONE;
    private FragmentType mPrevFragmentType = FragmentType.NONE;

    // Search: properties
    private StringBuilder mSearchQuery = new StringBuilder("");
    private SearchResult mSearchResult;
    private static FragmentType mSavedSearchFragment = FragmentType.NONE;
    private ResultItemInfo mSelectedRecyclePoint;

    // Search: getter-setter
    public String getSearchQuery() {
        return mSearchQuery.toString();
    }

    public void saveSearchFragment() {
        mSavedSearchFragment = mShownFragmentType;
    }

    public ResultItemInfo getSelectedRecyclePoint() {
        return mSelectedRecyclePoint;
    }

    public void setSelectedRecyclePoint(ResultItemInfo value) {
        mSelectedRecyclePoint = value;
    }

    public SearchResult getSearchResult() {
        return mSearchResult;
    }

    public void setSearchResult(SearchResult result) {
        mSearchResult = result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Helpers.startTimestamp();
        Log.i("BeautyAndroid", "Main activity started");

        super.onCreate(savedInstanceState);

        if(this.getSupportActionBar()!=null) {
            this.getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_main);

        // Only portrait orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mDatabase = FirebaseFirestore.getInstance();
        mSharedPref = getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        // Fragments: initialization

        // Add to the navigator the fragments and select the first one to show
        mNavigator.addFragment(mListFragment);
        mNavigator.addFragment(mDetailFragment);
        mNavigator.addFragment(mMapFragment);

        // Search: initialization

        // Get the intent, like a search, then verify the action and get the query
        handleIntent(getIntent());

        if (!mSearchQuery.toString().equals("")
            && mSavedSearchFragment == FragmentType.MAP) {
            Log.v("BeautyAndroid", "Show the map, as intent received from there");
            mNavigator.showFragment(mMapFragment);
            mShownFragmentType = FragmentType.MAP;
        } else {
            Log.v("BeautyAndroid", "Show the list, as no intent or one from another fragment than the map");
            mNavigator.showFragment(mListFragment);
            mShownFragmentType = FragmentType.LIST;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Fragments: methods
    public void navigate(FragmentType dest) {
        mPrevFragmentType = mShownFragmentType;
        mShownFragmentType = dest;
        onNavigation();
        mNavigator.showFragment(findFragment(dest));
    }

    public void navigateBack() {

        if (mPrevFragmentType == FragmentType.NONE) {
            Log.w("BeautyAndroid", "Cannot navigate back, as the previous fragment type is unknown");
            return;
        }

        FragmentType tmp = mPrevFragmentType;
        mPrevFragmentType = mShownFragmentType;
        mShownFragmentType = tmp;
        onNavigation();
        mNavigator.showFragment(findFragment(mShownFragmentType));
    }

    private Fragment findFragment(FragmentType type) {
        Fragment fragment;

        switch (type) {
            case LIST:
            default:
                fragment = mListFragment;
                break;
            case DETAIL:
                fragment = mDetailFragment;
                break;
            case MAP:
                fragment = mMapFragment;
                break;
        }

        return fragment;
    }

    private void onNavigation() {
        switch (mShownFragmentType) {
            case LIST:
                switch (mPrevFragmentType) {
                    case DETAIL:
                        CollectionPagerAdapter.setPage(0);
                        break;
                    case MAP:
                    default:
                        break;
                }
            case MAP:
                switch (mPrevFragmentType) {
                    case DETAIL:
                    case MAP:
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    public void showDialog(String text, String tag) {
        var dialogFragment = new FragmentHelpDialog(text);
        dialogFragment.show(findFragment(mShownFragmentType).getChildFragmentManager(), tag);
    }

    public void toggleTabSwiping(boolean enable) {
        // Enable or disable swiping gesture for the view pager
        var fragment =
            (FragmentTabView) FragmentManager.findFragment(findViewById(R.id.appPager));

        if (enable) {
            fragment.enableTabSwiping();
        } else {
            fragment.disableTabSwiping();
        }
    }

    // Search: methods
    private void handleIntent(Intent intent) {

        String intentAction = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(intentAction)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.v("BeautyAndroid", "Intent ACTION_SEARCH received by the main activity with the query: "
                + query);
            mSearchQuery.append(query);
        } else if (Intent.ACTION_VIEW.equals(intentAction)) {
            Log.v("BeautyAndroid", "Intent ACTION_VIEW received by the main activity");
            mSearchQuery.append("usr");
        } else {
            Log.d("BeautyAndroid", "Another intent received by the main activity: " + intentAction);
        }
    }
}