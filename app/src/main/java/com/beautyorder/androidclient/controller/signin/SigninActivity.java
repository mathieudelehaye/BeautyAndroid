//
//  Signin.java
//
//  Created by Mathieu Delehaye on 4/02/2023.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
//
//  Copyright © 2023 Mathieu Delehaye. All rights reserved.
//
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//  FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

package com.beautyorder.androidclient.controller.signin;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.beautyorder.androidclient.Helpers;
import com.beautyorder.androidclient.SigninDialogListener;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.TaskCompletionManager;
import com.beautyorder.androidclient.controller.signin.dialog.FragmentStartDialog;
import com.beautyorder.androidclient.model.AppUser;
import com.beautyorder.androidclient.model.UserInfoDBEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

public class SigninActivity extends ActivityWithStart implements SigninDialogListener {
    private FirebaseFirestore mDatabase;
    private StringBuilder mDeviceId;
    private StringBuilder mPrefUserId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mDatabase = FirebaseFirestore.getInstance();

        // Navigate to the App screen if there is a registered uid in the app preferences
        getPreferenceIds();
        String lastUId = mPrefUserId.toString();
        if (!lastUId.equals("") && Helpers.isEmail(lastUId)) {
            startAppWithUser(lastUId, AppUser.AuthenticationType.REGISTERED);
        }

        DialogFragment newFragment = new FragmentStartDialog();
        newFragment.show(getSupportFragmentManager(), "FragmentStartDialog");
    }

    @Override
    public void onDialogAnonymousSigninClick(DialogFragment dialog) {
        Log.v("BeautyAndroid", "Anonymous sign-in button pressed");
        dialog.dismiss();

        String anonymousUid = getAnonymousUidFromPreferences();
        if (!anonymousUid.equals("")) {
            // Reuse the anonymous uid if it already exists in the app preferences
            Log.v("BeautyAndroid", "Anonymous uid reused: " + anonymousUid);

            startAppWithUser(anonymousUid, AppUser.AuthenticationType.NOT_REGISTERED);
        } else {
            searchDBForAutoUserId();
        }
    }

    @Override
    public void onDialogRegisteredSigninClick(DialogFragment dialog) {
        Log.v("BeautyAndroid", "Registered sign-in button pressed");
        dialog.dismiss();
    }

    private void searchDBForAutoUserId() {

        // Query the database for an anonymous user with the same device id
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("userInfos")
            .whereEqualTo("device_id", mDeviceId.toString())
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("BeautyAndroid", "Unsuccessful search for a userInfos DB entry matching the "
                            + "device");
                    }

                    QuerySnapshot snapshot = task.getResult();

                    final var anonymousUId = new StringBuilder("");
                    if (snapshot.size() != 0) {
                        var userInfosEntry = snapshot.getDocuments().get(0);
                        String uid = userInfosEntry.getId();

                        if (!Helpers.isEmail(uid)) {
                            anonymousUId.append(uid);
                        }
                    }

                    if (anonymousUId.length() == 0) {
                        Log.v("BeautyAndroid", "No userInfos entry found in the DB for the device: "
                            + mDeviceId);

                        // Create an anonymous user
                        tryAndCreateAutoUserId();
                        return;
                    }

                    final String anonymousUidText = anonymousUId.toString();
                    Log.v("BeautyAndroid", "Anonymous uid read from the database: " + anonymousUidText
                        + ", matching the device id: " + mDeviceId);

                    setAnonymousUidToPreferences(anonymousUidText);

                    startAppWithUser(anonymousUidText, AppUser.AuthenticationType.NOT_REGISTERED);
                }
            });
    }

    private void tryAndCreateAutoUserId() {

        // Query the database
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("userInfos")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (!task.isSuccessful()) {
                        return;
                    }

                    // Get the user number already in the database
                    QuerySnapshot snapshot = task.getResult();
                    int userNumber = snapshot.size();

                    // Get the timestamp
                    Date date = new Date();
                    String time = String.valueOf(date.getTime());

                    // Compute the uid
                    String tmpId = mDeviceId.toString() + time + String.valueOf(userNumber);

                    byte[] hash = {};
                    StringBuilder uid = new StringBuilder("");

                    try {
                        MessageDigest md = MessageDigest.getInstance("SHA-1");

                        hash = md.digest(tmpId.getBytes(StandardCharsets.UTF_8));

                        uid.append(UUID.nameUUIDFromBytes(hash).toString());

                        // Add userInfos table entry to the database for the anonymous user
                        var userInfo = new UserInfoDBEntry(mDatabase, uid.toString());
                        userInfo.setScoreTime(UserInfoDBEntry.scoreTimeFormat.format(
                            UserInfoDBEntry.getDayBeforeDate(date)));
                        userInfo.setDeviceId(mSharedPref.getString(getString(R.string.device_id), ""));

                        userInfo.createAllDBFields(new TaskCompletionManager() {
                            @Override
                            public void onSuccess() {

                                String uidText = uid.toString();

                                Log.d("BeautyAndroid", "The user automatic identifier was created: "
                                        + uidText);

                                setAnonymousUidToPreferences(uidText);

                                startAppWithUser(uidText, AppUser.AuthenticationType.NOT_REGISTERED);
                            }

                            @Override
                            public void onFailure() {
                                // If the user id wasn't created in the database, try to generate another one
                                // and to write it again
                                tryAndCreateAutoUserId();
                            }
                        });
                    } catch (NoSuchAlgorithmException e) {
                        Log.e("BeautyAndroid", e.toString());
                    }
                }
            });
    }

    private void getPreferenceIds() {
        final Context ctxt = this;

        if (ctxt == null) {
            Log.w("BeautyAndroid", "No context to get the app preferences");
            return;
        }

        // Get the last uid
        mPrefUserId = new StringBuilder();
        mPrefUserId.append(mSharedPref.getString(getString(R.string.app_uid), ""));

        if (!mPrefUserId.toString().equals("")) {
            Log.v("BeautyAndroid", "Latest uid loaded from the app preferences: " + mPrefUserId.toString());
        }

        // Get the device id
        mDeviceId = new StringBuilder("");
        mDeviceId.append(mSharedPref.getString(getString(R.string.device_id), ""));

        if (!mDeviceId.toString().equals("")) {
            Log.v("BeautyAndroid", "The device id was read from the app preferences: " + mDeviceId.toString());
        } else {
            // If not found in the app preferences, read the device id and store it there
            readPhoneId(ctxt);
        }
    }

    private void readPhoneId(Context ctxt) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // From Android 10
            mDeviceId.append(Settings.Secure.getString(
                ctxt.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        } else {
            var telephonyManager = (TelephonyManager) ctxt.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager.getDeviceId() != null) {
                mDeviceId.append(telephonyManager.getDeviceId());
            } else {
                mDeviceId.append(Settings.Secure.getString(
                    ctxt.getContentResolver(),
                    Settings.Secure.ANDROID_ID));
            }
        }

        if (mDeviceId.toString().equals("")) {
            Log.e("BeautyAndroid", "Cannot determine the device id. Use a fake one instead");
            mDeviceId.append("1234");
            mSharedPref.edit().putString(getString(R.string.device_id), mDeviceId.toString()).commit();
        } else {
            mSharedPref.edit().putString(getString(R.string.device_id), mDeviceId.toString()).commit();
            Log.v("BeautyAndroid", "The device id was found on the device and written to the app "
                + "preferences: " + mDeviceId.toString());
        }
    }
}
