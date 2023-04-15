//
//  CircularKeyBuffer.java
//
//  Created by Mathieu Delehaye on 15/04/2023.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
//
//  Copyright © 2023 Mathieu Delehaye. All rights reserved.
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

import android.util.Log;
import androidx.annotation.NonNull;
import java.util.ArrayList;

// Circular buffer with unique items. E.g. to store the N most recent and non-duplicate search queries
public class CircularKeyBuffer<T> {
    private final int mSize;
    private final int mMaximumItemIndex;
    private ArrayList<T> mData = new ArrayList<>();
    private int mMostRecentItemIndex = -1;

    public CircularKeyBuffer(int size) {
        if (size <= 0) {
            Log.e("BeautyAndroid", "Cannot create a circular buffer smaller than 1");
            mSize = -1;
            mMaximumItemIndex = size - 1;
            return;
        }

        mSize = size;
        mMaximumItemIndex = size - 1;
    }

    public void add(@NonNull T item) {
        if (item.equals("") | item.equals(0)) {
            Log.w("BeautyAndroid", "Cannot add an empty item");
            return;
        }

        // Do not duplicate the stored items
        if (isDuplicate(item)) {
            return;
        }

        mMostRecentItemIndex++;
        if (mMostRecentItemIndex > mMaximumItemIndex) {
            mMostRecentItemIndex = 0;
        }

        if (mMostRecentItemIndex >= mData.size()) {
            mData.add(item);
        } else {
            mData.set(mMostRecentItemIndex, item);
        }
    }

    public int items() {
        return mData.size();
    }

    public T readFromEnd(Integer index) {
        final boolean atLeastFourDataItems = mData.size() > mMaximumItemIndex;

        if (!atLeastFourDataItems && index > mMostRecentItemIndex) {
            Log.w("BeautyAndroid", "Not possible to read the item, as the buffer doesn't have at least "
                + (mMostRecentItemIndex + 1) + " items");
            return null;
        }

        if (index > mMaximumItemIndex) {
            Log.w("BeautyAndroid", "Not possible to read the item, as the index is greater than the"
                + "maximum " + mMaximumItemIndex);
            return null;
        }

        int actualBufferIndex;
        final int queriesWithIndexSmallerOrEqualToTheMostRecent = mMostRecentItemIndex + 1;

        if (index >= queriesWithIndexSmallerOrEqualToTheMostRecent) {
            actualBufferIndex = mMaximumItemIndex + queriesWithIndexSmallerOrEqualToTheMostRecent - index;
        } else {
            actualBufferIndex = mMostRecentItemIndex - index;
        }

        return mData.get(actualBufferIndex);
    }

    private boolean isDuplicate(T item) {
        final boolean isItemOfTypeString = (item instanceof String);

        for (T pastItem : mData) {
            if (isItemOfTypeString ?
                ((String)pastItem).trim().equalsIgnoreCase(((String)item).trim()) :
                (pastItem == item)) {
                return true;
            }
        }
        return false;
    }
}
