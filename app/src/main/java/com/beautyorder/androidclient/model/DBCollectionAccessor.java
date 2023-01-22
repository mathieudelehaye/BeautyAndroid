package com.beautyorder.androidclient.model;

import android.util.Log;
import androidx.annotation.NonNull;
import com.beautyorder.androidclient.TaskCompletionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class DBCollectionAccessor {
    protected FirebaseFirestore mDatabase;
    protected String mCollectionName;
    protected StringBuilder mKey = new StringBuilder("");
    protected Map<String, String> mData;
    protected Map<String, Boolean> mDataChanged;

    public DBCollectionAccessor(FirebaseFirestore database, String collection) {
        mDatabase = database;
        mCollectionName = collection;
    }

    public void SetKey(String value) {
        mKey.setLength(0);
        mKey.append(value);
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
                    // Display score
                    Integer userScore = 0;
                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("BeautyAndroid", mKey.toString() + " => " + document.getData());

                            for (String field :fields) {
                                mData.put(field, document.getData().get(field).toString());
                                mDataChanged.put(field, false);
                            }

                            if (cbManager.length >= 1) {
                                cbManager[0].onSuccess();
                            }
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
}
