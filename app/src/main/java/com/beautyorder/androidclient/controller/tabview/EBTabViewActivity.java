//
//  EBTabViewActivity.java
//
//  Created by Mathieu Delehaye on 3/06/2023.
//
//  AndroidJavaTools: A framework to develop Android apps in Java.
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
//  You should have received a copy of the GNU Affero General Public License along with this program. If not, see
//  <https://www.gnu.org/licenses/>.

package com.beautyorder.androidclient.controller.tabview;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.android.java.androidjavatools.Helpers;
import com.android.java.androidjavatools.controller.tabview.TabViewActivity;
import com.android.java.androidjavatools.controller.Navigator;
import com.android.java.androidjavatools.controller.template.ResultProvider;
import com.android.java.androidjavatools.controller.template.SearchHistoryManager;
import com.android.java.androidjavatools.model.*;
import com.beautyorder.androidclient.controller.tabview.camera.EBFragmentCamera;
import com.beautyorder.androidclient.controller.tabview.product.EBFragmentProductDetail;
import com.beautyorder.androidclient.controller.tabview.product.EBFragmentProductSelection;
import com.beautyorder.androidclient.controller.tabview.profile.EBFragmentAccount;
import com.beautyorder.androidclient.controller.tabview.profile.EBFragmentHelp;
import com.beautyorder.androidclient.controller.tabview.profile.EBFragmentTerms;
import com.beautyorder.androidclient.controller.tabview.result.EBFragmentResultDetail;
import com.beautyorder.androidclient.controller.tabview.result.list.EBFragmentResultList;
import com.beautyorder.androidclient.controller.tabview.result.map.EBFragmentMap;
import com.beautyorder.androidclient.controller.tabview.search.EBFragmentSuggestion;
import com.beautyorder.androidclient.model.EBUserInfoDBEntry;
import com.beautyorder.androidclient.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.util.*;

public class EBTabViewActivity extends TabViewActivity implements ActivityWithAsyncTask,
    SearchHistoryManager, ResultProvider, Navigator.NavigatorManager {

    // Background: properties
    // TODO: do not use a static property here
    public static boolean scoreTransferredFromAnonymousAccount = false;
    private Set<String> mPhotoQueue;
    final private int mDelayBeforePhotoSendingInSec = 5;  // time in s to wait between two score writing attempts
    private final int mTimeBeforePollingScoreInMin = 1;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Set layout background
        final var coordinatorLayout = (CoordinatorLayout) findViewById(
            com.android.java.androidjavatools.R.id.main_activity_layout);
        coordinatorLayout.setBackground(getResources().getDrawable(R.drawable.background));

        // Set layout logo icon
        final var mainActivityIcon = (ImageView) findViewById(
            com.android.java.androidjavatools.R.id.main_activity_icon);
        mainActivityIcon.setImageResource(R.drawable.brand_logo);

        // Background: initialization
        var runner = new AsyncTaskRunner(this, mDatabase, mDelayBeforePhotoSendingInSec
            , 0);
        runner.execute(String.valueOf(mDelayBeforePhotoSendingInSec));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void createNavigator() {
        mNavigator = new Navigator(this, com.android.java.androidjavatools.R.id.main_activity_layout);

        mNavigator.createFragment("tab", EBFragmentTabView.class);
        mNavigator.createFragment("camera", EBFragmentCamera.class);
        mNavigator.createFragment("account", EBFragmentAccount.class);
        mNavigator.createFragment("help", EBFragmentHelp.class);
        mNavigator.createFragment("terms", EBFragmentTerms.class);
        mNavigator.createFragment("list", EBFragmentResultList.class);
        mNavigator.createFragment("map", EBFragmentMap.class);
        mNavigator.createFragment("detail", EBFragmentResultDetail.class);
        mNavigator.createFragment("suggestion", EBFragmentSuggestion.class);
        mNavigator.createFragment("products", EBFragmentProductSelection.class);
        mNavigator.createFragment("product", EBFragmentProductDetail.class);

        mNavigator.showFragment("tab");
    }

    public void showScore(int value) {
        Log.v("BeautyAndroid", "Show score: " + value);

        // TODO: fix the score display
//        TextView appScore = mTabViewFragment.getView().findViewById(R.id.score_text);
//        if (appScore != null) {
//            appScore.setText(value + " pts");
//        }
    }

    // Background: methods
    @Override
    public boolean environmentCondition() {
        if (!isNetworkAvailable()) {
            //Log.v("BeautyAndroid", "Try to write the scanning events but no network");
            return false;
        }

        if (AppUser.getInstance().getAuthenticationType() == AppUser.AuthenticationType.NONE) {
            //Log.v("BeautyAndroid", "Try to write the scanning events but no app user");
            return false;
        }

        mPhotoQueue = mSharedPref.getStringSet(getString(R.string.photos_to_send), new HashSet<>());
        if (mPhotoQueue.isEmpty()) {
            //Log.v("BeautyAndroid", "Try to write the scanning events but queue is empty");
            return false;
        }

        return true;
    }

    @Override
    public boolean timeCondition(long cumulatedTimeInSec) {

        final int secondsByMinute = 60; // change it to debug
        if ((cumulatedTimeInSec / secondsByMinute) < mTimeBeforePollingScoreInMin) {
            //Log.v("BeautyAndroid", "Timed condition not fulfilled: " + cumulatedTimeInSec
            //    + " sec out of " + (mTimeBeforePollingScoreInMin * secondsByMinute));
            return false;
        }

        //Log.v("BeautyAndroid", "Timed condition fulfilled");
        return true;
    }

    @Override
    public void runEnvironmentDependentActions() {

        Log.d("BeautyAndroid", "Number of events to send in the queue: " + mPhotoQueue.size());

        String uid = AppUser.getInstance().getId();

        // Process the first event in the queue: increment the score in the database then removing the event
        // from the queue
        for(String photoPath: mPhotoQueue) {

            var entry = new EBUserInfoDBEntry(mDatabase, uid);

            entry.readScoreDBFields(new TaskCompletionManager() {
                @Override
                public void onSuccess() {

                    // Get the date from the photo name
                    Date photoDate = Helpers.parseTime(EBUserInfoDBEntry.scoreTimeFormat,
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
                    mPhotoQueue.remove(photoPath);
                    mSharedPref.edit().putStringSet(getString(R.string.photos_to_send),
                        mPhotoQueue).commit();
                }

                @Override
                public void onFailure() {
                }
            });

            break;
        }
    }

    @Override
    public void runTimesDependentActions() {
        downloadScore();
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
                    + exception);
            }
        });
    }

    private void downloadScore() {

        //Log.v("BeautyAndroid", "Start to download the score");  // uncomment to debug

        String uid = AppUser.getInstance().getId();
        var entry = new EBUserInfoDBEntry(mDatabase, uid);

        entry.readScoreDBFields(new TaskCompletionManager() {
            @Override
            public void onSuccess() {

                final int downloadedScore = entry.getScore();
                String preferenceKey = getString(R.string.last_downloaded_score);
                final int preferenceScore =
                    mSharedPref.getInt(preferenceKey, 0);

                //Log.v("BeautyAndroid", "Score downloaded: current: " + preferenceScore + ", new: "
                //    + downloadedScore); // uncomment to debug

                if (preferenceScore < downloadedScore) {
                    Log.v("BeautyAndroid", "Shown score updated: " + downloadedScore);

                    showScore(downloadedScore);
                    // TODO: update the score download
//                    if (scoreTransferredFromAnonymousAccount) {
//                        scoreTransferredFromAnonymousAccount = false;
//                        showDialog(getString(R.string.score_transferred) + downloadedScore
//                            + "!", "Score transferred");
//                    } else  {
//                        showDialog(getString(R.string.new_score_displayed)+ downloadedScore, "Score increased");
//                    }
                }

                if (preferenceScore != downloadedScore) {
                    mSharedPref.edit().putInt(preferenceKey, downloadedScore).commit();
                }
            }

            @Override
            public void onFailure() {
            }
        });
    }
}