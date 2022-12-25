//
//  UserInfoEntry.java
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

package com.example.beautyandroid.model;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class UserInfoEntry {
    public interface CallbackManager {
        void onSuccess();
        void onFailure();
    }
    private FirebaseFirestore mDatabase;
    private String mKey;
    private Map<String, Object> mData;

    public UserInfoEntry(FirebaseFirestore _database, String _key, Map<String, Object> _data) {
        mDatabase = _database;
        mKey = _key;
        mData = _data;
    }

    public UserInfoEntry(FirebaseFirestore _database, String _key) {
        mDatabase = _database;
        mKey = _key;

        mData = new HashMap<>();
        mData.put("first_name", "");
        mData.put("last_name", "");
        mData.put("address", "");
        mData.put("city", "");
        mData.put("post_code", "");
        mData.put("score", 0);
    }

    public void writeToDatabase(CallbackManager... cbManager) {

        // Add userInfos table entry to the database matching the app user
        mDatabase.collection("userInfos").document(mKey)
            .set(mData)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("BeautyAndroid", "New info successfully written to the database for user: " + mKey);

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

    public int readScoreField(CallbackManager... cbManager) {

        mDatabase.collection("userInfos")
            .whereEqualTo("__name__", mKey)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    // Display score
                    Integer userScore = 0;

                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("BeautyAndroid", mKey + " => " + document.getData());

                            userScore = Integer.parseInt(document.getData().get("score").toString());

                            mData.put("score", userScore);

                            if (cbManager.length >= 1) {
                                cbManager[0].onSuccess();
                            }
                        }
                    } else {
                        Log.d("BeautyAndroid", "Error getting documents: ", task.getException());

                        if (cbManager.length >= 1) {
                            cbManager[0].onFailure();
                        }
                    }
                }
            });

        return 0;
    }

    public void incrementAndWriteScoreField() {
        // Get a new write batch
        WriteBatch batch = mDatabase.batch();

        DocumentReference ref = mDatabase.collection("userInfos").document(mKey);
        batch.update(ref, "score", (int)mData.get("score") + 1);

        // Commit the batch
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
    }
}
