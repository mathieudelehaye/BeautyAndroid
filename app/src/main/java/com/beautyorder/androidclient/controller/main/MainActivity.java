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

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.beautyorder.androidclient.Helpers;
import com.beautyorder.androidclient.model.AppUser;
import com.beautyorder.androidclient.model.ResultItemInfo;
import com.beautyorder.androidclient.model.ScoreTransferer;
import com.beautyorder.androidclient.model.UserInfoDBEntry;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.TaskCompletionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {
    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        private String resp;
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            try {
                int time = Integer.parseInt(params[0])*1000;

                Thread.sleep(time);
                resp = "Slept for " + params[0] + " seconds";
            } catch (InterruptedException e) {
                e.printStackTrace();
                resp = e.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            // Actions to execute after the background task
            // Return if there is no network available
            if (!isNetworkAvailable()) {
                //Log.v("BeautyAndroid", "Try to write the scanning events but no network");
                restart();
                return;
            }

            // Return if there is no app user yet
            if (AppUser.getInstance().getAuthenticationType() == AppUser.AuthenticationType.NONE) {
                //Log.v("BeautyAndroid", "Try to write the scanning events but no app user");
                restart();
                return;
            }

            // Return if there is no scanning events to send in the app preferences
            var scoreQueue = (HashSet<String>) mSharedPref.getStringSet(getString(R.string.eb_points_to_send),
                new HashSet<String>());

            if (scoreQueue.isEmpty()) {
                //Log.v("BeautyAndroid", "Try to write the scanning events but queue is empty");
                restart();
                return;
            }

            Log.d("BeautyAndroid", "Number of events to send in the queue: " + scoreQueue.size());

            HashSet<String> updatedQueue = (HashSet<String>)scoreQueue.clone();

            String uid = AppUser.getInstance().getId();

            // Process the first event in the queue: increment the score in the database then removing the event
            // from the queue
            for(String event: scoreQueue) {

                UserInfoDBEntry entry = new UserInfoDBEntry(mDatabase, uid);
                entry.readScoreDBFields(new TaskCompletionManager() {
                    @Override
                    public void onSuccess() {

                        Date eventDate = UserInfoDBEntry.parseScoreTime(event);

                        // Only update the score if the event date is after the DB score time
                        if (eventDate.compareTo(entry.getScoreTime()) > 0) {
                            final int newScore = entry.getScore() + 1;

                            entry.setScore(newScore);
                            entry.setScoreTime(event);
                            entry.updateDBFields(new TaskCompletionManager() {
                                @Override
                                public void onSuccess() {
                                    Log.v("BeautyAndroid", "Score written to the database and displayed "
                                        + "on screen");

                                    new ScoreTransferer(mDatabase, mThis).displayScoreOnScreen(newScore);
                                }

                                @Override
                                public void onFailure() {
                                }
                            });

                            Log.i("BeautyAndroid", "Scanning event sent to the database: " + event);
                        } else {
                            Log.w("BeautyAndroid", "Scanning event older than the latest in the database: " + event);
                        }

                        Log.d("BeautyAndroid", "Scanning event removed from the app queue: " + event);
                        updatedQueue.remove(event);
                        mSharedPref.edit().putStringSet(getString(R.string.eb_points_to_send), updatedQueue).commit();
                    }

                    @Override
                    public void onFailure() {
                    }
                });

                break;
            }

            restart();
        }

        @Override
        protected void onPreExecute() {
            // Actions to execute before the background task
        }

        @Override
        protected void onProgressUpdate(String... text) {
            // Publish progress
        }

        private void restart() {
            // Restart the asynchronous task
            var runner = new AsyncTaskRunner();
            runner.execute(mRunnerSleepTime.toString());
        }
    }

    // TODO: find another way to make `this` reference available to the nested async task class
    private MainActivity mThis;
    private SharedPreferences mSharedPref;
    private FirebaseFirestore mDatabase;
    private StringBuilder mSearchQuery = new StringBuilder("");
    private ResultItemInfo mSelectedRecyclePoint;
    private StringBuilder mRunnerSleepTime = new StringBuilder("");
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

        mRunnerSleepTime.append(String.valueOf(mDelayBetweenScoreWritingsInSec));
        var runner = new AsyncTaskRunner();
        runner.execute(mRunnerSleepTime.toString());

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

    private boolean isNetworkAvailable() {
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
}