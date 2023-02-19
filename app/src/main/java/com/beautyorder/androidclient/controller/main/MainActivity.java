//
//  MainActivity.java
//
//  Created by Mathieu Delehaye on 1/12/2022.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
//
//  Copyright © 2022 Mathieu Delehaye. All rights reserved.
//
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//  FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

package com.beautyorder.androidclient.controller.main;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import androidx.fragment.app.FragmentManager;
import com.beautyorder.androidclient.Helpers;
import com.beautyorder.androidclient.model.AsyncDBDataSender;
import com.beautyorder.androidclient.model.ResultItemInfo;
import com.beautyorder.androidclient.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    // TODO: find another way to make `this` reference available to the nested async task class
    private MainActivity mThis;
    private SharedPreferences mSharedPref;
    private FirebaseFirestore mDatabase;
    private StringBuilder mSearchQuery = new StringBuilder("");
    private ResultItemInfo mSelectedRecyclePoint;
    final private int mDelayBetweenScoreWritingsInSec = 5;  // time in s to wait between two score writing attempts

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Helpers.startTimestamp();
        Log.i("BeautyAndroid", "Main activity started");

        super.onCreate(savedInstanceState);

        if(this.getSupportActionBar()!=null) {
            this.getSupportActionBar().hide();
        }

        mSharedPref = this.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);

        // Only portrait orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mThis = this;
        mDatabase = FirebaseFirestore.getInstance();

        var runner = new AsyncDBDataSender(this, mDatabase, mDelayBetweenScoreWritingsInSec);
        runner.execute(String.valueOf(mDelayBetweenScoreWritingsInSec));

        // Get the intent, like a search, then verify the action and get the query
        handleIntent(getIntent());
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

    public String getSearchQuery() {
        return mSearchQuery.toString();
    }

    public ResultItemInfo getSelectedRecyclePoint() {
        return mSelectedRecyclePoint;
    }

    public void setSelectedRecyclePoint(ResultItemInfo value) {
        mSelectedRecyclePoint = value;
    }

    public boolean isNetworkAvailable() {
        var connectivityManager
            = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

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

    public void enableTabSwiping() {
        // Enable swiping gesture for the view pager
        var fragment =
            (FragmentApp) FragmentManager.findFragment(findViewById(R.id.appPager));
        fragment.enableTabSwiping();
    }

    public void disableTabSwiping() {
        // Disable the swiping gesture for the view pager
        var fragment =
            (FragmentApp) FragmentManager.findFragment(findViewById(R.id.appPager));
        fragment.disableTabSwiping();
    }
}