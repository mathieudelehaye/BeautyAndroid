//
//  ScoreUpdater.java
//
//  Created by Mathieu Delehaye on 3/01/2023.
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

package com.beautyorder.androidclient.model;

import android.util.Log;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.controller.main.FragmentMap;
import com.beautyorder.androidclient.controller.main.MainActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.beautyorder.androidclient.TaskCompletionManager;

import java.util.Date;

public class ScoreUpdater {
    private FirebaseFirestore mDatabase;
    private MainActivity mActivity;
    private String mSourceUid;
    private String mDestinationUid;

    public ScoreUpdater(FirebaseFirestore _database, String _sourceUid, String _destinationUid,
        MainActivity _activity) {

        mDatabase = _database;
        mSourceUid = _sourceUid;
        mDestinationUid = _destinationUid;
        mActivity = _activity;
    }

    public ScoreUpdater(FirebaseFirestore _database, MainActivity _activity) {

        mDatabase = _database;
        mSourceUid = "";
        mDestinationUid = "";
        mActivity = _activity;
    }

    public void run() {
        checkAndTransferScoreFromAnonymousUser();
    }

    private void checkAndTransferScoreFromAnonymousUser() {

        if (!mSourceUid.equals("")) {
            // An anonymous uid already was found in the app preferences
            Log.d("BeautyAndroid", "Try to transfer score from the anonymous uid found in the app preferences: "
                + mSourceUid);

            // Get the DB
            mDatabase = FirebaseFirestore.getInstance();

            com.beautyorder.androidclient.model.UserInfoEntry anonymousUserEntry =
                new com.beautyorder.androidclient.model.UserInfoEntry(mDatabase, mSourceUid);
            anonymousUserEntry.readScoreDBFields(new TaskCompletionManager() {
                @Override
                public void onSuccess() {

                    if (anonymousUserEntry.getScore() > 0) {
                        Log.v("BeautyAndroid", "Anonymous user data read from the database: "
                            + String.valueOf(anonymousUserEntry.getScore()));

                        clearAndTransferScoreFromAnonymousUser(anonymousUserEntry);
                    }
                }

                @Override
                public void onFailure() {
                }
            });
        }
    }

    private void clearAndTransferScoreFromAnonymousUser(
        com.beautyorder.androidclient.model.UserInfoEntry anonymousUserEntry) {

        // Clear the anonymous user score in the DB
        final int anonymousUserScore = anonymousUserEntry.getScore();
        final Date anonymousUserTimestamp = anonymousUserEntry.getScoreTime();

        anonymousUserEntry.setScore(0);
        anonymousUserEntry.setScoreTime(com.beautyorder.androidclient.model.UserInfoEntry.scoreTimeFormat.format(
            com.beautyorder.androidclient.model.UserInfoEntry.getDayBeforeDate(new Date())));

        anonymousUserEntry.updateDBFields(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                Log.v("BeautyAndroid", "Anonymous user data cleared in the database");

                readAndAddToRegisteredUserScore(anonymousUserScore, anonymousUserTimestamp);
            }

            @Override
            public void onFailure() {
            }
        });
    }

    private void readAndAddToRegisteredUserScore(int scoreToAddValue, Date scoreToAddTimestamp) {

        // Read the registered user info from the DB
        com.beautyorder.androidclient.model.UserInfoEntry registeredUserEntry =
            new com.beautyorder.androidclient.model.UserInfoEntry(mDatabase, mDestinationUid.toString());
        registeredUserEntry.readScoreDBFields(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                final int registeredUserScore = registeredUserEntry.getScore();

                Log.v("BeautyAndroid", "Registered user data read from the database: "
                    + String.valueOf(registeredUserScore));

                if (scoreToAddTimestamp.compareTo(registeredUserEntry.getScoreTime()) < 0) {
                    // If the score to add date is older than the registered user score date, add the former to the
                    // registered user score.

                    updateUserScoreInDatabase(registeredUserEntry,
                            registeredUserScore + scoreToAddValue);
                } else {
                    // Otherwise, add the (anonymous score - 1) to the registered one

                    updateUserScoreInDatabase(registeredUserEntry,
                        registeredUserEntry.getScore() + scoreToAddValue);
                }
            }

            @Override
            public void onFailure() {
            }
        });
    }

    private void updateUserScoreInDatabase(com.beautyorder.androidclient.model.UserInfoEntry userEntry, int newScore) {
        // Clear the anonymous user score in the DB
        userEntry.setScore(newScore);
        userEntry.updateDBFields(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                Log.d("BeautyAndroid", "Registered user score updated in the database to: "
                    + String.valueOf(newScore));

                displayScoreOnScreen(newScore);
            }

            @Override
            public void onFailure() {
            }
        });
    }

    public void displayScoreOnScreen(int value) {
        if (mActivity == null) {
            return;
        }

        Log.v("BeautyAndroid", "Display score on screen: " + String.valueOf(value));
        com.beautyorder.androidclient.controller.main.FragmentMap fragment =
            (com.beautyorder.androidclient.controller.main.FragmentMap) FragmentManager.findFragment(mActivity.findViewById(R.id.mapScore));
        TextView score = (TextView) fragment.getView().findViewById(R.id.mapScore);
        score.setText(String.valueOf(value) + " pts");
    }
}
