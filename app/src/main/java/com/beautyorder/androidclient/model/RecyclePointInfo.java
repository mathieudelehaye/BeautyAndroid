//
//  RecyclePointInfo.java
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

import com.beautyorder.androidclient.TaskCompletionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Map;

public class RecyclePointInfo extends DBCollectionAccessor {

    public RecyclePointInfo(FirebaseFirestore database) {

        super(database, "recyclePointInfos");

        mData = new ArrayList<Map<String, String>>();
        mDataChanged = new ArrayList<Map<String, Boolean>>();
    }

    public boolean readAllDBFields(String[] outputFields, TaskCompletionManager... cbManager) {
        return readDBFieldsForCurrentFilter(outputFields, cbManager);
    }
}
