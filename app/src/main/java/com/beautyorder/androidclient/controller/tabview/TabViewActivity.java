//
//  TabViewActivity.java
//
//  Created by Mathieu Delehaye on 1/12/2022.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
//
//  Copyright © 2022 Mathieu Delehaye. All rights reserved.
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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import com.android.java.androidjavatools.Helpers;
import com.android.java.androidjavatools.controller.tabview.Navigator;
import com.android.java.androidjavatools.controller.tabview.search.FragmentWithSearch;
import com.android.java.androidjavatools.model.*;
import com.beautyorder.androidclient.*;
import com.android.java.androidjavatools.controller.tabview.result.FragmentResult;
import com.beautyorder.androidclient.controller.tabview.camera.EBFragmentCamera;
import com.beautyorder.androidclient.controller.tabview.menu.EBFragmentHelp;
import com.beautyorder.androidclient.controller.tabview.menu.EBFragmentMenu;
import com.beautyorder.androidclient.controller.tabview.menu.EBFragmentTerms;
import com.beautyorder.androidclient.controller.tabview.result.EBFragmentResultDetail;
import com.beautyorder.androidclient.controller.tabview.result.list.EBFragmentResultList;
import com.beautyorder.androidclient.controller.tabview.result.map.EBFragmentMap;
import com.beautyorder.androidclient.controller.tabview.search.EBFragmentSuggestion;
import com.beautyorder.androidclient.model.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.util.*;

public class TabViewActivity extends AppCompatActivity implements ActivityWithAsyncTask,
    FragmentWithSearch.SearchHistoryManager, FragmentResult.ResultProvider, Navigator.NavigatorManager {

    private FirebaseFirestore mDatabase;
    private SharedPreferences mSharedPref;

    // Fragments: properties
    private Navigator mNavigator;

    // Search: properties
    private final int mSavedListMaxSize = 100;
    private HashMap<String, ResultItemInfo> mPastRP = new HashMap<>();
    private HashMap<String, ResultItemInfo> mSavedRP = new HashMap<>();
    private ArrayList<String> mSavedRPKeys = new ArrayList<>();
    private CircularKeyBuffer<String> mPastRPKeys = new CircularKeyBuffer<>(2);
    private CircularKeyBuffer<String> mPastSearchQueries = new CircularKeyBuffer<>(4);
    private SearchResult mSearchResult = new SearchResult();
    private String mSelectedResultItemKey = "";

    // Background: properties
    // TODO: do not use a static property here
    public static boolean scoreTransferredFromAnonymousAccount = false;
    private Set<String> mPhotoQueue;
    final private int mDelayBeforePhotoSendingInSec = 5;  // time in s to wait between two score writing attempts
    private final int mTimeBeforePollingScoreInMin = 1;

    // Search: getter-setter
    public ResultItemInfo getSelectedResultItem() {
        return mSearchResult.get(mSelectedResultItemKey);
    }

    public void setSelectedResultItem(ResultItemInfo value) {
        final String key = value.getKey();

        mSelectedResultItemKey = key;

        if (!mPastRP.containsKey(key)) {
            mPastRP.put(key, value);
        }

        if (!key.equals("")) {
            mPastRPKeys.add(key);
        }
    }

    public SearchResult getSearchResult() {
        return mSearchResult;
    }

    @Override
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

        mNavigator = new Navigator(this, R.id.mainActivityLayout);
        mNavigator.createFragment("tab", EBFragmentTabView.class);
        mNavigator.createFragment("camera", EBFragmentCamera.class);
        mNavigator.createFragment("menu", EBFragmentMenu.class);
        mNavigator.createFragment("help", EBFragmentHelp.class);
        mNavigator.createFragment("terms", EBFragmentTerms.class);
        mNavigator.createFragment("list", EBFragmentResultList.class);
        mNavigator.createFragment("map", EBFragmentMap.class);
        mNavigator.createFragment("detail", EBFragmentResultDetail.class);
        mNavigator.createFragment("suggestion", EBFragmentSuggestion.class);
        mNavigator.showFragment("tab");

        // TODO: uncomment and update logic to process the query
//        String intentAction = intent.getAction();
//        if (Intent.ACTION_SEARCH.equals(intentAction)) {
//            String query = intent.getStringExtra(SearchManager.QUERY);
//            Log.v("BeautyAndroid", "Intent ACTION_SEARCH received by the main activity with the query: "
//                    + query);
//            mSearchQuery.append(query);
//        } else if (Intent.ACTION_VIEW.equals(intentAction)) {
//            Log.v("BeautyAndroid", "Intent ACTION_VIEW received by the main activity");
//            mSearchQuery.append("usr");
//        } else {
//            Log.d("BeautyAndroid", "Another intent received by the main activity: " + intentAction);
//        }

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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Search: methods
    public int getPreviousQueryNumber() {
        return mPastSearchQueries.items();
    }

    @Override
    public String getPreviousSearchQuery(int index) {
        return mPastSearchQueries.getFromEnd(index);
    }

    @Override
    public int getPreviousResultNumber() {
        return mPastRPKeys.items();
    }

    @Override
    public ResultItemInfo getPreviousResultItem(int index) {
        final String key = mPastRPKeys.getFromEnd(index);

        return mPastRP.get(key);
    }

    @Override
    public int getSavedResultItemNumber() {
        return mSavedRP.size();
    }

    @Override
    public ResultItemInfo getSavedResultItem(int index) {
        if (index > mSavedRP.size()) {
            Log.e("BeautyAndroid", "Saved result index greater than actual list size");
            return null;
        }

        return mSavedRP.get(mSavedRPKeys.get(index));
    }

    @Override
    public boolean createSavedResult(ResultItemInfo value) {
        if (mSavedRPKeys.size() >= mSavedListMaxSize) {
            Log.w("BeautyAndroid", "Cannot save more than " + mSavedListMaxSize + " RP");
            return false;
        }

        final String key = value.getKey();
        mSavedRP.put(key, value);
        mSavedRPKeys.add(key);

        return true;
    }

    @Override
    public boolean isSavedResult(String key) {
        return mSavedRP.containsKey(key);
    }

    @Override
    public void deleteSavedResult(String key) {
        mSavedRP.remove(key);

        for (int i = 0; i < mSavedRPKeys.size(); i++) {
            if (mSavedRPKeys.get(i).equals(key)) {
                mSavedRPKeys.remove(i);
                break;
            }
        }
    }

    @Override
    public void storeSearchQuery(@NonNull String query) {
        mPastSearchQueries.add(query);
    }

    // Fragments: methods

    public void toggleToolbar(Boolean visible) {
        Log.v("BeautyAndroid", "Toolbar visibility toggled to " + visible);
        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        mainToolbar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public Navigator navigator() {
        return mNavigator;
    }

    @Override
    public void onNavigation(@NonNull String dest, @NonNull String orig) {
        switch (dest) {
            case "tab":
                switch (orig) {
                    case "help":
                    case "terms":
                        CollectionPagerAdapter.setPage(2);
                        break;
                    case "suggestion":
                        // Show toolbar when coming from the Suggestion page
                        toggleToolbar(true);
                        break;
                    default:
                        break;
                }
                break;
            case "list":
            case "map":
                switch (orig) {
                    case "suggestion":
                        // Show toolbar when coming from the Suggestion page
                        toggleToolbar(true);
                        break;
                    default:
                        break;
                }
                break;
            case "suggestion":
                // Hide toolbar when going to the Suggestion page
                toggleToolbar(false);
            default:
                break;
        }
    }

    public void showScore(int value) {
        Log.v("BeautyAndroid", "Show score: " + value);

        // TODO: fix the score display
//        TextView appScore = mTabViewFragment.getView().findViewById(R.id.score_text);
//        if (appScore != null) {
//            appScore.setText(value + " pts");
//        }
    }

    @Override
    public void toggleTabSwiping(boolean enable) {
        mNavigator.toggleTabSwiping(enable);
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