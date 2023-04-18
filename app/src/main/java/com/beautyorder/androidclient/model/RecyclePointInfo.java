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
//  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
//  Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
//  warranty of MERCHANTABILITY or FITNESS
//  FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this program. If not, see
//  <https://www.gnu.org/licenses/>.

package com.beautyorder.androidclient.model;

import com.android.java.androidjavatools.model.DBCollectionAccessor;
import com.android.java.androidjavatools.model.TaskCompletionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class RecyclePointInfo extends DBCollectionAccessor {

    public RecyclePointInfo(FirebaseFirestore database) {

        super(database, "recyclePointInfos");

        mData = new ArrayList<>();
        mDataChanged = new ArrayList<>();
    }

    public String getTitleAtIndex(int i) {
        String pointName = mData.get(i).get("PointName");
        return ((pointName != null)
            && !pointName.equals("?")) ? (pointName + " ") : "";
    }

    public String getSnippetAtIndex(int i) {
        String threeWords = mData.get(i).get(("3Words"));
        String recyclingProgram = mData.get(i).get("RecyclingProgram");

        return getAddressAtIndex(i) +
            ((threeWords != null)
                && !threeWords.equals("?") ? ("\n(https://what3words.com/"
                + threeWords + ")") : "") +
            ((recyclingProgram != null)
                && !recyclingProgram.equals("?") ? ("\n\nBrands: " + recyclingProgram) : "");
    }

    public String getKeyAtIndex(int i) {
        return mData.get(i).get("key");
    }

    public String getAddressAtIndex(int i) {
        String buildingName = mData.get(i).get("BuildingName");
        String buildingNumber = mData.get(i).get("BuildingNumber");
        String address = mData.get(i).get("Address");
        String postcode = mData.get(i).get("Postcode");
        String city = mData.get(i).get("City");

        return ((buildingName != null)
            && !buildingName.equals("?")  ? (buildingName + " ") : "") +
            ((buildingNumber != null)
                && !buildingNumber.equals("?") ? (buildingNumber + ", ") : "") +
            ((address != null)
                && !address.equals("?") ? (address + " ") : "") +
            ((postcode != null)
                && !postcode.equals("?") ? (postcode + " ") : "") +
            ((city != null)
                && !city.equals("?") ? (city + " ") : "");
    }

    public String getImageUrlAtIndex(int i) {
        String pointImage = mData.get(i).get("ImageUrl");
        return ((pointImage != null)
            && !pointImage.equals("?")) ? pointImage : "";
    }

    public double getLatitudeAtIndex(int i) {
        return Double.parseDouble(mData.get(i).get("Latitude"));
    }

    public double getLongitudeAtIndex(int i) {
        return Double.parseDouble(mData.get(i).get("Longitude"));
    }

    public boolean readAllDBFields(String[] outputFields, TaskCompletionManager... cbManager) {
        return readDBFieldsForCurrentFilter(outputFields, cbManager);
    }
}
