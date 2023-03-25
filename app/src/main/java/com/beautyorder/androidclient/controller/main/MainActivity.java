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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.beautyorder.androidclient.*;
import com.beautyorder.androidclient.controller.Navigator;
import com.beautyorder.androidclient.controller.main.dialog.FragmentHelpDialog;
import com.beautyorder.androidclient.controller.results.list.FragmentResultDetail;
import com.beautyorder.androidclient.controller.results.map.FragmentMap;
import com.beautyorder.androidclient.model.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ActivityWithAsyncTask {
   // Fragments: types
    public enum FragmentType {
        APP,
        HELP,
        TERMS,
        DETAIL,
        MAP,
        NONE
    }

    private FirebaseFirestore mDatabase;
    private SharedPreferences mSharedPref;

    // Fragments: properties
    private Navigator mNavigator = new Navigator(this);
    private FragmentMain mAppFragment = new FragmentMain();
    private FragmentHelp mHelpFragment = new FragmentHelp();
    private FragmentTerms mTermsFragment = new FragmentTerms();
    private FragmentResultDetail mDetailFragment = new FragmentResultDetail();
    private FragmentMap mMapFragment = new FragmentMap();
    private FragmentType mShownFragmentType = FragmentType.NONE;
    private FragmentType mPrevFragmentType = FragmentType.NONE;

    // Search: properties
    private StringBuilder mSearchQuery = new StringBuilder("");
    private SearchResult mSearchResult;
    private static FragmentType mSavedSearchFragment = FragmentType.NONE;
    private ResultItemInfo mSelectedRecyclePoint;

    // Background: properties
    // TODO: do not use a static property here
    public static boolean scoreTransferredFromAnonymousAccount = false;
    private Set<String> mPhotoQueue;
    final private int mDelayBeforePhotoSendingInSec = 5;  // time in s to wait between two score writing attempts
    private final int mTimeBeforePollingScoreInMin = 1;

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
        mNavigator.addFragment(mAppFragment);
        mNavigator.addFragment(mHelpFragment);
        mNavigator.addFragment(mTermsFragment);
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
            mNavigator.showFragment(mAppFragment);
            mShownFragmentType = FragmentType.APP;
        }

        // Background: initialization
        var runner = new AsyncTaskRunner(this, mDatabase, mDelayBeforePhotoSendingInSec
            , 0);
        runner.execute(String.valueOf(mDelayBeforePhotoSendingInSec));
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
            case APP:
            default:
                fragment = mAppFragment;
                break;
            case HELP:
                fragment = mHelpFragment;
                break;
            case TERMS:
                fragment = mTermsFragment;
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
            case APP:
                switch (mPrevFragmentType) {
                    case DETAIL:
                        CollectionPagerAdapter.setPage(0);
                        break;
                    case HELP:
                    case TERMS:
                        CollectionPagerAdapter.setPage(2);
                        break;
                    case APP:
                    case MAP:
                    default:
                        break;
                }
            case MAP:
                switch (mPrevFragmentType) {
                    case APP:
                        mMapFragment.toggleDetailsView(false);
                        break;
                    case DETAIL:
                    case HELP:
                    case TERMS:
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

    public void showScore(int value) {
        Log.v("BeautyAndroid", "Show score: " + value);

        TextView appScore = mAppFragment.getView().findViewById(R.id.score_text);
        if (appScore != null) {
            appScore.setText(value + " pts");
        }

        TextView mapScore = mMapFragment.getView().findViewById(R.id.score_text);
        if (mapScore != null) {
            mapScore.setText(value + " pts");
        }
    }

    public void toggleTabSwiping(boolean enable) {
        // Enable or disable swiping gesture for the view pager
        var fragment =
            (FragmentMain) FragmentManager.findFragment(findViewById(R.id.appPager));

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

    // Background task: methods
    private boolean isNetworkAvailable() {
        var connectivityManager
            = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = (connectivityManager != null) ?
            connectivityManager.getActiveNetworkInfo() : null;
        return (activeNetworkInfo != null) && activeNetworkInfo.isConnected();
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
        var entry = new UserInfoDBEntry(mDatabase, uid);

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
                    if (scoreTransferredFromAnonymousAccount) {
                        scoreTransferredFromAnonymousAccount = false;
                        showDialog(getString(R.string.score_transferred) + downloadedScore
                            + "!", "Score transferred");
                    } else  {
                        showDialog(getString(R.string.new_score_displayed)+ downloadedScore, "Score increased");
                    }
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