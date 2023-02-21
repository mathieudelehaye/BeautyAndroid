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

                        // Upload the photo to the Cloud Storage for Firebase
                        var photoFile = new File(photoPath);
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
                                    + String.valueOf(Helpers.getTimestamp()));

                                if (!photoFile.delete()) {
                                    Log.w("BeautyAndroid", "Unable to delete the local photo file: "
                                        + photoPath);
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
        var runner = new AsyncDBDataSender(mActivity, mDatabase, mSleepTime);
        runner.execute(String.valueOf(mSleepTime));
    }

    private void sendImage(File file) {

        // Upload the file to the Cloud Storage for Firebase
        final var fileURI = Uri.fromFile(file);

        StorageReference riversRef = (FirebaseStorage.getInstance().getReference())
            .child("user_images/"+fileURI.getLastPathSegment());

        UploadTask uploadTask = riversRef.putFile(fileURI);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                Log.d("BeautyAndroid", "Image uploaded to the database");
                Log.v("BeautyAndroid", "Image uploaded to the database at timestamp: "
                        + String.valueOf(Helpers.getTimestamp()));

                if (!file.delete()) {
                    Log.w("BeautyAndroid", "Unable to delete the local image: "
                            + file);
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
