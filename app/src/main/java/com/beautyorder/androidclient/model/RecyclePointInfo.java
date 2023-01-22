package com.beautyorder.androidclient.model;

import android.util.Log;
import androidx.annotation.NonNull;
import com.beautyorder.androidclient.TaskCompletionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class RecyclePointInfo {

//    private FirebaseFirestore mDatabase;
//
//    public RecyclePointInfo(FirebaseFirestore database) {
//        mDatabase = database;
//    }
//
//    public void read(TaskCompletionManager... cbManager) {
//
//        // Add userInfos table entry to the database matching the app user
//        mDatabase.collection("userInfos").document(mKey)
//            .set(mData)
//            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//                    Log.i("BeautyAndroid", "New info successfully written to the database for user: " + mKey);
//
//                    if (cbManager.length >= 1) {
//                        cbManager[0].onSuccess();
//                    }
//                }
//            })
//            .addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Log.e("BeautyAndroid", "Error writing user info to the database: ", e);
//
//                    if (cbManager.length >= 1) {
//                        cbManager[0].onFailure();
//                    }
//                }
//            });
//    }
}
