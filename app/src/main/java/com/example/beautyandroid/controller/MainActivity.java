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

package com.example.beautyandroid.controller;

import android.app.ProgressDialog;
import android.content.Context;
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
import com.beautyorder.androidclient.R;
import com.example.beautyandroid.TaskCompletionManager;
import com.example.beautyandroid.model.AppUser;
import com.example.beautyandroid.model.UserInfoEntry;
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

            // Return of there is no scanning events to send in the app preferences
            HashSet<String> scoreQueue = (HashSet<String>) mSharedPref.getStringSet(getString(R.string.scores_to_send),
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

                UserInfoEntry entry = new UserInfoEntry(mDatabase, uid);
                entry.readScoreDBFields(new TaskCompletionManager() {
                    @Override
                    public void onSuccess() {

                        Date eventDate = UserInfoEntry.parseScoreTime(event);

                        // Only update the score if the event date is after the DB score time
                        if (eventDate.compareTo(entry.getScoreTime()) > 0) {
                            entry.setScore(entry.getScore() + 1);
                            entry.setScoreTime(event);
                            entry.updateScoreDBFields();
                            Log.i("BeautyAndroid", "Scanning event sent to the database: " + event);
                        } else {
                            Log.w("BeautyAndroid", "Scanning event older than the latest in the database: " + event);
                        }

                        Log.d("BeautyAndroid", "Scanning event removed from the app queue: " + event);
                        updatedQueue.remove(event);
                        mSharedPref.edit().putStringSet(getString(R.string.scores_to_send), updatedQueue).commit();
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
            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute(mRunnerSleepTime.toString());
        }
    }

    private SharedPreferences mSharedPref;
    private FirebaseFirestore mDatabase;
    private StringBuilder mRunnerSleepTime = new StringBuilder("");
    final private int mDelayBetweenScoreWritingsInSec = 5;  // time in s to wait between two score writing attempts

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(this.getSupportActionBar()!=null) {
            this.getSupportActionBar().hide();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Only portrait orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mSharedPref = this.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        // Get the DB
        mDatabase = FirebaseFirestore.getInstance();

        mRunnerSleepTime.append(String.valueOf(mDelayBetweenScoreWritingsInSec));
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(mRunnerSleepTime.toString());
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
            = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}