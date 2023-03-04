//
//  AsyncDBDataConnector.java
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
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.NonNull;
import com.beautyorder.androidclient.Helpers;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.TaskCompletionManager;
import com.beautyorder.androidclient.controller.main.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.util.Date;
import java.util.HashSet;

public class AsyncDBDataConnector extends AsyncTask<String, String, String> {
    private Activity mActivity;
    private SharedPreferences mSharedPref;
    private FirebaseFirestore mDatabase;
    private int mSleepTimeInSec;
    private long mCumulatedSleepTimeInSec = 0;
    private final int mTimeBeforePollingScoreInMin = 1;

    public AsyncDBDataConnector(Activity activity, FirebaseFirestore database, int sleepTime,
        long cumulatedSleepTime) {

        super();

        mActivity = activity;

        mSharedPref = activity.getSharedPreferences(
            activity.getString(R.string.app_name), Context.MODE_PRIVATE);

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

        mCumulatedSleepTimeInSec += mSleepTimeInSec;

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

        downloadScore();

        // Return if there is no scanning events to send in the app preferences
        var photoQueue = (HashSet<String>) mSharedPref.getStringSet(mActivity.getString(R.string.photos_to_send),
            new HashSet<>());

        if (photoQueue.isEmpty()) {
            //Log.v("BeautyAndroid", "Try to write the scanning events but queue is empty");
            restart();
            return;
        }

        Log.d("BeautyAndroid", "Number of events to send in the queue: " + photoQueue.size());

        String uid = AppUser.getInstance().getId();

        // Process the first event in the queue: increment the score in the database then removing the event
        // from the queue
        for(String photoPath: photoQueue) {

            var entry = new UserInfoDBEntry(mDatabase, uid);

            entry.readScoreDBFields(new TaskCompletionManager() {
                @Override
                public void onSuccess() {

                    // Get the date from the photo name
                    Date photoDate = Helpers.parseTime(UserInfoDBEntry.scoreTimeFormat,
                        photoPath.substring(photoPath.lastIndexOf("-") + 1));

                    // Only update the score if the event date is after the DB score time
                    if (photoDate.compareTo(entry.getScoreTime()) > 0) {

                        // The app won't increase the score, as the photo must first be verified at the backend
                        // server

                        uploadPhoto(photoPath);
                    } else {
                        Log.w("BeautyAndroid", "Photo older than the latest in the database: " + photoPath);
                    }

                    Log.d("BeautyAndroid", "Photo removed from the app queue: " + photoPath);
                    photoQueue.remove(photoPath);
                    mSharedPref.edit().putStringSet(mActivity.getString(R.string.photos_to_send),
                        photoQueue).commit();
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
        var runner = new AsyncDBDataConnector(mActivity, mDatabase, mSleepTimeInSec, mCumulatedSleepTimeInSec);
        runner.execute(String.valueOf(mSleepTimeInSec));
    }

    private void downloadScore() {
        if ((mCumulatedSleepTimeInSec / 60) < mTimeBeforePollingScoreInMin) {
            return;
        }

        mCumulatedSleepTimeInSec = 0;

        String uid = AppUser.getInstance().getId();
        var entry = new UserInfoDBEntry(mDatabase, uid);

        entry.readScoreDBFields(new TaskCompletionManager() {
            @Override
            public void onSuccess() {

                final int downloadedScore = entry.getScore();
                String preferenceKey = mActivity.getString(R.string.last_downloaded_score);
                final int preferenceScore =
                    mSharedPref.getInt(preferenceKey, 0);

                if (preferenceScore < downloadedScore) {
                    Log.v("BeautyAndroid", "Score updated from database: " + downloadedScore);

                    mSharedPref.edit().putInt(preferenceKey, downloadedScore).commit();

                    var mainActivity = (MainActivity) mActivity;
                    if (mainActivity != null) {
                        mainActivity.showScore(downloadedScore);
                        mainActivity.showDialog("Your score has been increased to "
                            + downloadedScore, "Score increased");
                    }
                }
            }

            @Override
            public void onFailure() {
            }
        });
    }

    private void uploadPhoto(String path) {
        // Upload the photo to the Cloud Storage for Firebase

        var photoFile = new File(path);
        final var photoURI = Uri.fromFile(photoFile);

        StorageReference riversRef = (FirebaseStorage.getInstance().getReference())
            .child("user_images/"+photoURI.getLastPathSegment());

        UploadTask uploadTask = riversRef.putFile(photoURI);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                Log.i("BeautyAndroid", "Photo sent to the database");
                Log.v("BeautyAndroid", "Photo uploaded to the database at timestamp: "
                    + Helpers.getTimestamp());

                if (!photoFile.delete()) {
                    Log.w("BeautyAndroid", "Unable to delete the local photo file: "
                        + path);
                } else {
                    Log.v("BeautyAndroid", "Local image successfully deleted");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.e("BeautyAndroid", "Failed to upload the image with the error:"
                    + exception.toString());
            }
        });
    }
}
