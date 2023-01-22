//
//  UserInfoDBEntry.java
//
//  Created by Mathieu Delehaye on 24/12/2022.
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
import androidx.annotation.NonNull;
import com.beautyorder.androidclient.TaskCompletionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.Map;

public class UserInfoDBEntry extends DBCollectionAccessor {
    static public SimpleDateFormat scoreTimeFormat = new SimpleDateFormat("yyyy.MM.dd");
    private Date mScoreTime;

    public UserInfoDBEntry(FirebaseFirestore database, String key, Map<String, String> data) {

        super(database, "userInfos");

        mKey.append(key);
        mData = data;
        mScoreTime = parseScoreTime((String)data.get("score_time"));

        initializeDataChange();
    }

    public UserInfoDBEntry(FirebaseFirestore database, String key) {

        super(database, "userInfos");

        mKey.append(key);

        mData = new HashMap<>();
        mData.put("first_name", "");
        mData.put("last_name", "");
        mData.put("address", "");
        mData.put("city", "");
        mData.put("post_code", "");
        mData.put("score", "");
        mData.put("score_time", "1970.01.01");
        mData.put("device_id", "");
        mScoreTime = parseScoreTime("1970.01.01");

        initializeDataChange();
    }

    private void initializeDataChange() {
        mDataChanged = new HashMap<>();
        mDataChanged.put("first_name", false);
        mDataChanged.put("last_name", false);
        mDataChanged.put("address", false);
        mDataChanged.put("city", false);
        mDataChanged.put("post_code", false);
        mDataChanged.put("score", false);
        mDataChanged.put("score_time", false);
        mDataChanged.put("device_id", false);
    }

    public int getScore() {
        String score = mData.get("score");
        return (score != "") ? (int)Integer.parseInt(score) : 0;
    }

    public void setScore(int value) {
        mData.put("score", String.valueOf(value));
        mDataChanged.put("score", true);
    }

    public Date getScoreTime() {
        return parseScoreTime((String)mData.get("score_time"));
    }

    public void setScoreTime(String value) {
        mScoreTime = parseScoreTime(value);
        mData.put("score_time", value);
        mDataChanged.put("score_time", true);
    }

    public String getDeviceId() {
        return (String)mData.get("device_id");
    }

    public void setDeviceId(String value) {
        mData.put("device_id", value);
        mDataChanged.put("device_id", true);
    }

    public void createAllDBFields(TaskCompletionManager... cbManager) {

        // Add userInfos table entry to the database matching the app user
        mDatabase.collection("userInfos").document(mKey.toString())
            .set(mData)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("BeautyAndroid", "New info successfully written to the database for user: "
                        + mKey.toString());

                    if (cbManager.length >= 1) {
                        cbManager[0].onSuccess();
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("BeautyAndroid", "Error writing user info to the database: ", e);

                    if (cbManager.length >= 1) {
                        cbManager[0].onFailure();
                    }
                }
            });
    }

    public boolean readScoreDBFields(TaskCompletionManager... cbManager) {
        String[] fields = {"score", "score_time"};
        return readDBFieldsForCurrentKey(fields, cbManager);
    }

    public void updateDBFields(TaskCompletionManager... cbManager) {
        // Get a new write batch
        WriteBatch batch = mDatabase.batch();

        DocumentReference ref = mDatabase.collection("userInfos").document(mKey.toString());

        var changedKeys = new ArrayList<String>();

        for (String key : mData.keySet()) {
            if (mDataChanged.get(key)) {
                // Data has changed and must be written back to the database
                batch.update(ref, key, mData.get(key));
                changedKeys.add(key);
            }
        }

        // Commit the batch
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    // Clear the update flag
                    for (String key: changedKeys) {
                        mDataChanged.put(key, false);
                    }

                    if (cbManager.length >= 1) {
                        cbManager[0].onSuccess();
                    }
                } else {
                    Log.e("BeautyAndroid", "Error updating documents: ", task.getException());

                    if (cbManager.length >= 1) {
                        cbManager[0].onFailure();
                    }
                }
            }
        });
    }

    static public Date parseScoreTime(String scoreTime) {
        try {
            return scoreTimeFormat.parse(scoreTime);
        } catch (ParseException e) {
            Log.e("BeautyAndroid", "Error while parsing the score date from database: "
                + e.toString());

            return new Date();
        }
    }

    static public Date getDayBeforeDate(Date date) {
        return new java.util.Date(date.getTime() - 1000 * 60 * 60 * 24); // ms in 1 day
    }
}
