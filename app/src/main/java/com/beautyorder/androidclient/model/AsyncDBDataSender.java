//
//  AsyncDBDataSender.java
//
//  Created by Mathieu Delehaye on 19/02/2023.
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

package com.beautyorder.androidclient.model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.TaskCompletionManager;
import com.beautyorder.androidclient.controller.main.MainActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashSet;

public class AsyncDBDataSender extends AsyncTask<String, String, String> {
    private Activity mActivity;
    private SharedPreferences mSharedPref;
    private FirebaseFirestore mDatabase;
    private int mSleepTime;

    public AsyncDBDataSender(Activity activity, FirebaseFirestore database, int sleepTime) {
        super();

        mActivity = activity;

        mSharedPref = activity.getSharedPreferences(
            activity.getString(R.string.app_name), Context.MODE_PRIVATE);

        mDatabase = database;

        mSleepTime = sleepTime;
    }

    @Override
    protected String doInBackground(String... params) {
        var response = new StringBuilder();

        publishProgress("Sleeping..."); // Calls onProgressUpdate()
        try {
            int time = Integer.parseInt(params[0])*1000;

            Thread.sleep(time);
            response.append("Slept for " + params[0] + " seconds");
        } catch (InterruptedException e) {
            e.printStackTrace();
            response.append(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.append(e.getMessage());
        }

        return response.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        // Actions to execute after the background task
        // Return if there is no network available
        if (!((MainActivity)mActivity).isNetworkAvailable()) {
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
        var scoreQueue = (HashSet<String>) mSharedPref.getStringSet(mActivity.getString(R.string.eb_points_to_send),
            new HashSet<String>());

        if (scoreQueue.isEmpty()) {
            //Log.v("BeautyAndroid", "Try to write the scanning events but queue is empty");
            restart();
            return;
        }

        Log.d("BeautyAndroid", "Number of events to send in the queue: " + scoreQueue.size());

        var updatedQueue = (HashSet<String>)scoreQueue.clone();

        String uid = AppUser.getInstance().getId();

        // Process the first event in the queue: increment the score in the database then removing the event
        // from the queue
        for(String event: scoreQueue) {

            var entry = new UserInfoDBEntry(mDatabase, uid);

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

                                new ScoreTransferer(mDatabase, (MainActivity) mActivity)
                                    .displayScoreOnScreen(newScore);
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
                    mSharedPref.edit().putStringSet(mActivity.getString(R.string.eb_points_to_send),
                        updatedQueue).commit();
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
        var runner = new AsyncDBDataSender(mActivity, mDatabase, mSleepTime);
        runner.execute(String.valueOf(mSleepTime));
    }
}
