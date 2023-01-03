//
//  UserScoreTransferer.java
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

package com.example.beautyandroid.model;

import android.util.Log;
import com.example.beautyandroid.TaskCompletionManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class UserScoreTransferer {
    private FirebaseFirestore mDatabase;
    private String mSourceUid;
    private String mDestinationUid;

    public UserScoreTransferer(FirebaseFirestore _database, String _sourceUid, String _destinationUid) {
        mDatabase = _database;
        mSourceUid = _sourceUid;
        mDestinationUid = _destinationUid;
    }

    public void run() {
        checkAndTransferScoreFromAnonymousUser();
    }

    private void checkAndTransferScoreFromAnonymousUser() {

        if (!mSourceUid.equals("")) {
            // An anonymous uid already was found in the app preferences
            Log.v("BeautyAndroid", "Try to transfer score from the anonymous uid found in the app preferences: "
                + mSourceUid);

            // Get the DB
            mDatabase = FirebaseFirestore.getInstance();

            UserInfoEntry anonymousUserEntry = new UserInfoEntry(mDatabase, mSourceUid);
            anonymousUserEntry.readScoreDBFields(new TaskCompletionManager() {
                @Override
                public void onSuccess() {

                    if (anonymousUserEntry.getScore() > 0) {

                        clearAndTransferFromAnonymousUser(anonymousUserEntry);
                    }
                }

                @Override
                public void onFailure() {
                }
            });
        }
    }

    private void clearAndTransferFromAnonymousUser(UserInfoEntry anonymousUserEntry) {

        // Clear the anonymous user score in the DB
        anonymousUserEntry.setScore(0);
        anonymousUserEntry.setScoreTime(UserInfoEntry.scoreTimeFormat.format(
                UserInfoEntry.getDayBeforeDate(new Date())));
        anonymousUserEntry.updateScoreDBFields(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                readAndAddToRegisteredUser(anonymousUserEntry.getScore(), anonymousUserEntry.getScoreTime());
            }

            @Override
            public void onFailure() {
            }
        });
    }

    private void readAndAddToRegisteredUser(int scoreToAddValue, Date scoreToAddTimestamp) {

        // Read the registered user info from the DB
        UserInfoEntry registeredUserEntry = new UserInfoEntry(mDatabase, mDestinationUid.toString());
        registeredUserEntry.readScoreDBFields(new TaskCompletionManager() {
            @Override
            public void onSuccess() {

                if (scoreToAddTimestamp.compareTo(registeredUserEntry.getScoreTime()) < 0) {
                    // If the score to add date is older than the registered user score date, add the former to the
                    // registered user score.

                    updateUserScore(registeredUserEntry,
                            registeredUserEntry.getScore() + scoreToAddValue);
                } else {
                    // Otherwise, add the (anonymous score - 1) to the registered one

                    updateUserScore(registeredUserEntry,
                            registeredUserEntry.getScore() + scoreToAddValue - 1);
                }
            }

            @Override
            public void onFailure() {
            }
        });
    }

    private void updateUserScore(UserInfoEntry userEntry, int newScore) {
        // Clear the anonymous user score in the DB
        userEntry.setScore(0);
        userEntry.updateScoreDBFields(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure() {
            }
        });
    }
}
