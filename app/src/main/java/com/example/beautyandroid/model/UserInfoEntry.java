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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class UserInfoEntry {
    private FirebaseFirestore mDatabase;
    private String mKey;
    private Map<String, Object> mData;

    public UserInfoEntry(FirebaseFirestore _database, String _key, Map<String, Object> _data) {
        mDatabase = _database;
        mKey = _key;
        mData = _data;
    }

    public void writeToDatabase() {

        // Add userInfos table entry to the database matching the app user
        mDatabase.collection("userInfos").document(mKey)
            .set(mData)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("BeautyAndroid", "New info successfully written to the database for user: " + mKey);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("BeautyAndroid", "Error writing to the database", e);
                }
            });
    }
}
