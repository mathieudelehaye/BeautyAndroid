//
//  DBCollectionAccessor.java
//
//  Created by Mathieu Delehaye on 22/01/2023.
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DBCollectionAccessor {
    private class SearchFilter {
        private String[] mFields;
        private double[] mMinRanges;
        private double[] mMaxRanges;

        public SearchFilter(String[] fields, double[] minRanges, double[] maxRanges) {
            mFields = fields;
            mMinRanges = minRanges;
            mMaxRanges = maxRanges;
        }

        public int getRanges() {
            return mMaxRanges.length;
        }

        public String getFieldAtIndex(int i) {
            return mFields[i];
        }

        public double getMinRangeAtIndex(int i) {
            return mMinRanges[i];
        }

        public double getMaxRangeAtIndex(int i$) {
            return mMaxRanges[i$];
        }
    }

    protected FirebaseFirestore mDatabase;
    protected String mCollectionName;
    protected StringBuilder mKey = new StringBuilder("");
    protected SearchFilter mFilter;

    // TODO: use a polymorphic type for `mData` and `mDataChanged` in order to avoid the map list.
    protected ArrayList<Map<String, String>> mData;
    protected ArrayList<Map<String, Boolean>> mDataChanged;

    public ArrayList<Map<String, String>> getData() {
        return mData;
    }

    public DBCollectionAccessor(FirebaseFirestore database, String collection) {
        mDatabase = database;
        mCollectionName = collection;
    }

    public void SetKey(String value) {
        mKey.setLength(0);
        mKey.append(value);
    }

    public void SetFilter(String[] fields, double[] minRanges, double[] maxRanges) {
        mFilter = new SearchFilter(fields, minRanges, maxRanges);
    }

    public boolean readDBFieldsForCurrentKey(String[] fields, TaskCompletionManager... cbManager) {

        if (mKey == null || mKey.toString().equals("")) {
            Log.w("BeautyAndroid", "Try to read with no valid key the fields from the DB collection: "
                + mCollectionName);
            return false;
        }

        mDatabase.collection(mCollectionName)
            .whereEqualTo("__name__", mKey.toString())
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {

                        readResultFields(task.getResult(), fields, null);

                        if (cbManager.length >= 1) {
                            cbManager[0].onSuccess();
                        }
                    } else {
                        Log.e("BeautyAndroid", "Error reading documents: ", task.getException());

                        if (cbManager.length >= 1) {
                            cbManager[0].onFailure();
                        }
                    }
                }
            });

        return true;
    }

    public boolean readDBFieldsForCurrentFilter(String[] fields, TaskCompletionManager... cbManager) {

        if (mFilter == null && mFilter.getRanges() < 1) {
            Log.w("BeautyAndroid", "Try to read with no valid filter the fields from the DB collection: "
                + mCollectionName);
            return false;
        }

        String firstFilterField = mFilter.getFieldAtIndex(0);

        mDatabase.collection(mCollectionName)
            .whereLessThan(firstFilterField, mFilter.getMaxRangeAtIndex(0))
            .whereGreaterThan(firstFilterField, mFilter.getMinRangeAtIndex(0))
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {

                        readResultFields(task.getResult(), fields, mFilter);

                        if (cbManager.length >= 1) {
                            cbManager[0].onSuccess();
                        }
                    } else {
                        Log.e("BeautyAndroid", "Error reading documents: ", task.getException());

                        if (cbManager.length >= 1) {
                            cbManager[0].onFailure();
                        }
                    }
                }
            });

        return true;
    }

    private void readResultFields(QuerySnapshot result, String[] fields, SearchFilter filter) {

        for (QueryDocumentSnapshot document : result) {

            // Possibly filter out the document
            if (filter != null && !filterDocument(document, filter)) {
                continue;
            }

            //Log.v("BeautyAndroid", document.getId() + " => " + document.getData());

            var dataItem = new HashMap<String, String>();
            var dataChangeItem = new HashMap<String, Boolean>();
            mData.add(dataItem);
            mDataChanged.add(dataChangeItem);

            for (String field :fields) {
                final Object fieldObject = document.getData().get(field);
                dataItem.put(field, (fieldObject != null) ? fieldObject.toString() : "");
                dataChangeItem.put(field, false);
            }
        }
    }

    private boolean filterDocument(QueryDocumentSnapshot document, SearchFilter filter) {
        // The filter item at index 0 has been used for the query
        for(int i = 1; i < filter.getRanges(); i++) {
            final var fieldValue = (double)document.getData().get(filter.getFieldAtIndex(i));

            if (fieldValue < filter.getMinRangeAtIndex(i) || fieldValue > filter.getMaxRangeAtIndex(i)) {
                return false;
            }
        }

        return true;
    }
}
