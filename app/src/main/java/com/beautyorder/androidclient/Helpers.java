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
import android.util.Log;
import android.util.Patterns;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Helpers {

    private static long mStartTimestamp = 0;

    // String
    public static boolean isEmail(String text) {
        return (!TextUtils.isEmpty((CharSequence) text) && Patterns.EMAIL_ADDRESS
            .matcher((CharSequence) text).matches());
    }

    public static boolean isEmpty(String text) {
        return TextUtils.isEmpty((CharSequence)text);
    }

    // Time
    public static void startTimestamp() {
        mStartTimestamp = (new Date()).getTime();
    }

    public static long getTimestamp() {
        return ((new Date()).getTime() - mStartTimestamp);
    }

    public static Date parseTime(SimpleDateFormat format, String time) {
        try {
            return format.parse(time);
        } catch (ParseException e) {
            Log.e("BeautyAndroid", "Error while parsing the time from database: "
                + e.toString());

            return new Date();
        }
    }

    public static Date getDayBeforeDate(Date date) {
        return new java.util.Date(date.getTime() - 1000 * 60 * 60 * 24);    // ms in 1 day
    }

    public static int compareYearDays(Date d1, Date d2) {
        if (d1.getYear() != d2.getYear()) {
            return d1.getYear() - d2.getYear();
        }

        if (d1.getMonth() != d2.getMonth()) {
            return d1.getMonth() - d2.getMonth();
        }

        return (d1.getDate() - d2.getDate());
    }

    // TODO: improve implementation, e.g. by using varargs or array for the called method arguments.
    public static <T, T1, T2, T3> Object callObjectMethod(Object obj, Class<T> objType, String methodName,
        T1 arg1, T2 arg2, T3 arg3) {

        if (obj == null) {
            Log.e("BeautyAndroid", "Cannot call method, as object null");
            return null;
        }

        var typedObject = objType.cast(obj);
        if (typedObject == null) {
            Log.e("BeautyAndroid", "Cannot call method, as object not of the expected type");
            return null;
        }

        try {
            Method method = (arg1 == null) ? typedObject.getClass().getMethod(methodName):
                (arg2 == null) ? typedObject.getClass().getMethod(methodName, arg1.getClass()):
                    (arg3 == null) ? typedObject.getClass().getMethod(methodName, arg1.getClass(), arg2.getClass()):
                        typedObject.getClass().getMethod(methodName, arg1.getClass(), arg2.getClass(),
                            arg3.getClass());

            Object ret = (arg1 == null) ? method.invoke(obj):
                (arg2 == null) ? method.invoke(obj, arg1):
                    (arg3 == null) ? method.invoke(obj, arg1, arg2):
                        method.invoke(obj, arg1, arg2, arg3);

            return ret;
        } catch (SecurityException e) {
            Log.e("BeautyAndroid", "Security exception: " + e);
            return null;
        } catch (NoSuchMethodException e) {
            Log.e("BeautyAndroid", "No method exception: " + e);
            return null;
        } catch (IllegalArgumentException e) {
            Log.e("BeautyAndroid", "Illegal argument method exception: " + e);
            return null;
        } catch (IllegalAccessException e) {
            Log.e("BeautyAndroid", "Illegal access exception: " + e);
            return null;
        } catch (InvocationTargetException e) {
            Log.e("BeautyAndroid", "Invocation target exception: " + e);
            return null;
        }
    }
}
