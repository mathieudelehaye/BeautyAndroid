//
//  AsyncTaskRunner.java
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

package com.beautyorder.androidclient;

import android.app.Activity;
import android.os.AsyncTask;
import com.google.firebase.firestore.FirebaseFirestore;

public class AsyncTaskRunner extends AsyncTask<String, String, String> {
    private ActivityWithAsyncTask mParentActivity;
    private FirebaseFirestore mDatabase;
    private int mSleepTimeInSec;
    private long mCumulatedSleepTimeInSec = 0;

    public AsyncTaskRunner(ActivityWithAsyncTask activityWithBgTask, FirebaseFirestore database,
        int sleepTime, long cumulatedSleepTime) {

        super();

        mParentActivity = activityWithBgTask;

        var activity = (Activity) activityWithBgTask;   // activityWithBgTask must be of class Activity

        mDatabase = database;

        mSleepTimeInSec = sleepTime;

        mCumulatedSleepTimeInSec = cumulatedSleepTime;
    }

    @Override
    protected String doInBackground(String... params) {
        var response = new StringBuilder();

        publishProgress("Sleeping..."); // Calls onProgressUpdate()

        try {
            int timeInMs = Integer.parseInt(params[0])*1000;

            Thread.sleep(timeInMs);
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
        mCumulatedSleepTimeInSec += mSleepTimeInSec;

        if(mParentActivity.environmentCondition()) {
           mParentActivity.runEnvironmentDependentActions();
        }

        if(mParentActivity.timeCondition(mCumulatedSleepTimeInSec)) {
            mCumulatedSleepTimeInSec = 0;
            mParentActivity.runTimesDependentActions();
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
        var runner = new AsyncTaskRunner(mParentActivity, mDatabase, mSleepTimeInSec, mCumulatedSleepTimeInSec);
        runner.execute(String.valueOf(mSleepTimeInSec));
    }
}
