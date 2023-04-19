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
import com.android.java.androidjavatools.model.DBCollectionAccessor;
import com.android.java.androidjavatools.model.TaskCompletionManager;
import com.android.java.androidjavatools.Helpers;
import com.google.firebase.firestore.*;
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
        mData = new ArrayList<>();
        mData.add(data);

        mScoreTime = Helpers.parseTime(scoreTimeFormat, (String)data.get("score_time"));

        initializeDataChange();
    }

    public UserInfoDBEntry(FirebaseFirestore database, String key) {

        super(database, "userInfos");

        mKey.append(key);

        mData = new ArrayList<>();
        var dataItem = new HashMap<String, String>();
        mData.add(dataItem);
        dataItem.put("first_name", "");
        dataItem.put("last_name", "");
        dataItem.put("address", "");
        dataItem.put("city", "");
        dataItem.put("post_code", "");
        dataItem.put("score", "");
        dataItem.put("score_time", "1970.01.01");
        dataItem.put("device_id", "");
        mScoreTime = Helpers.parseTime(scoreTimeFormat, "1970.01.01");

        initializeDataChange();
    }

    private void initializeDataChange() {
        mDataChanged = new ArrayList<>();
        var dataChangeItem = new HashMap<String, Boolean>();
        mDataChanged.add(dataChangeItem);
        dataChangeItem.put("first_name", false);
        dataChangeItem.put("last_name", false);
        dataChangeItem.put("address", false);
        dataChangeItem.put("city", false);
        dataChangeItem.put("post_code", false);
        dataChangeItem.put("score", false);
        dataChangeItem.put("score_time", false);
        dataChangeItem.put("device_id", false);
    }

    public int getScore() {
        String score = mData.get(0).get("score");
        return (score != "") ? Integer.parseInt(score) : 0;
    }

    public void setScore(int value) {
        mData.get(0).put("score", String.valueOf(value));
        mDataChanged.get(0).put("score", true);
    }

    public Date getScoreTime() {
        return Helpers.parseTime(scoreTimeFormat, (String)mData.get(0).get("score_time"));
    }

    public void setScoreTime(String value) {
        mScoreTime = Helpers.parseTime(scoreTimeFormat, value);
        mData.get(0).put("score_time", value);
        mDataChanged.get(0).put("score_time", true);
    }

    public String getDeviceId() {
        return (String)mData.get(0).get("device_id");
    }

    public void setDeviceId(String value) {
        mData.get(0).put("device_id", value);
        mDataChanged.get(0).put("device_id", true);
    }

    public void createAllDBFields(TaskCompletionManager... cbManager) {

        // Add userInfos table entry to the database matching the app user
        mDatabase.collection("userInfos").document(mKey.toString())
            .set(mData.get(0))
            .addOnSuccessListener(aVoid -> {
                Log.i("BeautyAndroid", "New info successfully written to the database for user: "
                        + mKey.toString());

                if (cbManager.length >= 1) {
                    cbManager[0].onSuccess();
                }
            })
            .addOnFailureListener(e -> {
                Log.e("BeautyAndroid", "Error writing user info to the database: ", e);

                if (cbManager.length >= 1) {
                    cbManager[0].onFailure();
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

        for (String key : mData.get(0).keySet()) {
            if (mDataChanged.get(0).get(key)) {
                // Data has changed and must be written back to the database
                batch.update(ref, key, mData.get(0).get(key));
                changedKeys.add(key);
            }
        }

        // Commit the batch
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                // Clear the update flag
                for (String key : changedKeys) {
                    mDataChanged.get(0).put(key, false);
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
        });
    }
}
