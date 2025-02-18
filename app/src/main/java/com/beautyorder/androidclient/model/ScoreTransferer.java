//
//  ScoreTransferer.java
//
//  Created by Mathieu Delehaye on 3/01/2023.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
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

package com.beautyorder.androidclient.model;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.android.java.androidjavatools.Helpers;
import com.android.java.androidjavatools.model.TaskCompletionManager;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.controller.tabview.EBTabViewActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;

public class ScoreTransferer {
    private FirebaseFirestore mDatabase;
    private Activity mActivity;
    private String mSourceUid;
    private String mDestinationUid;

    public ScoreTransferer(FirebaseFirestore _database, String _sourceUid, String _destinationUid,
       Activity _activity) {

        mDatabase = _database;
        mSourceUid = _sourceUid;
        mDestinationUid = _destinationUid;
        mActivity = _activity;
    }

    public ScoreTransferer(FirebaseFirestore _database, EBTabViewActivity _activity) {

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
            Log.d("EBT", "Try to transfer score from the anonymous uid found in the app preferences: "
                + mSourceUid);

            // Get the DB
            mDatabase = FirebaseFirestore.getInstance();

            var anonymousUserEntry = new EBUserInfoDBEntry(mDatabase, mSourceUid);
            anonymousUserEntry.readScoreDBFields(new TaskCompletionManager() {
                @Override
                public void onSuccess() {

                    if (anonymousUserEntry.getScore() > 0) {
                        Log.v("EBT", "Anonymous user data read from the database: "
                            + anonymousUserEntry.getScore());

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
        EBUserInfoDBEntry anonymousUserEntry) {

        // Clear the anonymous user score in the DB
        final int anonymousUserScore = anonymousUserEntry.getScore();
        final Date anonymousUserTimestamp = anonymousUserEntry.getScoreTime();

        anonymousUserEntry.setScore(0);
        anonymousUserEntry.setScoreTime(EBUserInfoDBEntry.scoreTimeFormat.format(
            Helpers.getDayBeforeDate(new Date())));

        anonymousUserEntry.updateDBFields(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                Log.v("EBT", "Anonymous user data cleared in the database");

                readAndAddToRegisteredUserScore(anonymousUserScore, anonymousUserTimestamp);
            }

            @Override
            public void onFailure() {
            }
        });
    }

    private void readAndAddToRegisteredUserScore(int scoreToAddValue, Date scoreToAddTimestamp) {

        // Read the registered user info from the DB
        var registeredUserEntry = new EBUserInfoDBEntry(mDatabase, mDestinationUid);
        registeredUserEntry.readScoreDBFields(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                final int registeredUserScore = registeredUserEntry.getScore();

                Log.v("EBT", "Registered user data read from the database: "
                    + registeredUserScore);

                if (scoreToAddTimestamp.compareTo(registeredUserEntry.getScoreTime()) < 0) {
                    // If the score to add date is after the registered user score date, add the former score
                    // to the latter one.

                    updateUserScoreInDatabase(registeredUserEntry, registeredUserScore + scoreToAddValue,
                        scoreToAddTimestamp);
                } else {
                    // Otherwise, add the (anonymous score - 1) to the registered one

                    updateUserScoreInDatabase(registeredUserEntry,
                        registeredUserEntry.getScore() + scoreToAddValue, scoreToAddTimestamp);
                }
            }

            @Override
            public void onFailure() {
            }
        });
    }

    private void updateUserScoreInDatabase(EBUserInfoDBEntry userEntry, int newScore, Date newTimestamp) {
        // Clear the anonymous user score in the DB
        userEntry.setScore(newScore);
        userEntry.setScoreTime(EBUserInfoDBEntry.scoreTimeFormat.format(newTimestamp));

        userEntry.updateDBFields(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                Log.d("EBT", "Registered user score updated in the database to: "
                    + newScore);

                // Reset the score in the app preferences, so it can be shown after downloading it.
                String preferenceKey = mActivity.getString(R.string.last_downloaded_score);
                mActivity.getSharedPreferences("Beauty-Android", Context.MODE_PRIVATE).edit()
                    .putInt(preferenceKey, 0).commit();
                Log.v("EBT", "Score reset in the app preferences");

                // TODO: do not use an static property here
                EBTabViewActivity.scoreTransferredFromAnonymousAccount = true;
            }

            @Override
            public void onFailure() {
            }
        });
    }
}
