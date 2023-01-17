//
//  FirebaseFirestoreMockManager.java
//
//  Created by Mathieu Delehaye on 8/01/2023.
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;
import com.google.firebase.firestore.WriteBatch;
import org.mockito.ArgumentMatchers;
import static org.mockito.Mockito.*;

public class FirebaseFirestoreMockManager {

    private FirebaseFirestore mDatabase;
    private CollectionReference mColRef;
    private DocumentReference mDocRef;
    private Task<Void> mTask;
    private WriteBatch mWriteBatch;

    public FirebaseFirestoreMockManager() {
        mDatabase = mock(FirebaseFirestore.class);;
        mColRef = mock(CollectionReference.class);
        mDocRef = mock(DocumentReference.class);
        mTask = (Task<Void>) mock(Task.class);
        mWriteBatch = mock(WriteBatch.class);

        when(mDatabase.collection(any(String.class))).thenReturn(mColRef);
        when(mDatabase.batch()).thenReturn(mWriteBatch);

        when(mColRef.document(any(String.class))).thenReturn(mDocRef);

        when(mDocRef.set(ArgumentMatchers.<Map<String, Object>>any())).thenReturn(mTask);

        when(mTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(mTask);
        when(mTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(mTask);
        when(mTask.addOnCompleteListener(any(OnCompleteListener.class))).thenReturn(mTask);

        when(mWriteBatch.update(any(DocumentReference.class), any(String.class), any(Object.class)))
            .thenReturn(mWriteBatch);
        when(mWriteBatch.commit()).thenReturn(mTask);
    }

    public FirebaseFirestore getDatabase() {
        return mDatabase;
    }
}
