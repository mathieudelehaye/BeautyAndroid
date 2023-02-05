//
//  Helpers.java
//
//  Created by Mathieu Delehaye on 27/12/2022.
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

package com.beautyorder.androidclient;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.Date;

public class Helpers {

    private static long mStartTimestamp = 0;

    public static boolean isEmail(String text) {
        return (!TextUtils.isEmpty((CharSequence) text) && Patterns.EMAIL_ADDRESS
            .matcher((CharSequence) text).matches());
    }

    public static boolean isEmpty(String text) {
        return TextUtils.isEmpty((CharSequence)text);
    }

    public static void startTimestamp() {
        mStartTimestamp = (new Date()).getTime();
    }

    public static long getTimestamp() {
        return ((new Date()).getTime() - mStartTimestamp);
    }
}
