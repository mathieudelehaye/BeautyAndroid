//
//  FragmentHome.java
//
//  Created by Mathieu Delehaye on 1/12/2022.
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

package com.example.beautyandroid.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.databinding.FragmentHomeBinding;
import com.example.beautyandroid.Helpers;
import com.example.beautyandroid.TaskCompletionManager;
import com.example.beautyandroid.model.AppUser;
import com.example.beautyandroid.model.UserInfoEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class FragmentHome extends FragmentWithStart {

    private FragmentHomeBinding binding;
    private FirebaseFirestore mDatabase;
    private SharedPreferences mSharedPref;
    private StringBuilder mPrefUserId;
    private StringBuilder mDeviceId;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseFirestore.getInstance();

        // Navigate to the App screen if there is an uid in the app preferences
        getPreferenceIds();
        if (!mPrefUserId.toString().equals("")) {

            String uid = mPrefUserId.toString();
            AppUser.AuthenticationType type = Helpers.isEmail(uid) ? AppUser.AuthenticationType.REGISTERED
                : AppUser.AuthenticationType.NOT_REGISTERED;

            startAppWithUser(mSharedPref, R.id.action_HomeFragment_to_AppFragment, uid, type);
        }

        binding.noChoiceHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String anonymousUid = getAnonymousUidFromPreferences(mSharedPref);
                if (!anonymousUid.equals("")) {
                    // Reuse the anonymous uid if it already exists in the app preferences
                    Log.v("BeautyAndroid", "Anonymous uid loaded from the app preferences and reused: "
                        + anonymousUid);

                    startAppWithUser(mSharedPref, R.id.action_HomeFragment_to_AppFragment, anonymousUid,
                        AppUser.AuthenticationType.NOT_REGISTERED);
                } else {
                    // Otherwise, create an anonymous uid
                    tryAndCreateAutoUserId();
                }
            }
        });

        binding.choice1Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FragmentHome.this)
                    .navigate(R.id.action_HomeFragment_to_LoginFragment);
            }
        });

        binding.choice2Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FragmentHome.this)
                    .navigate(R.id.action_HomeFragment_to_RegisterFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
                    String tmpId = mDeviceId + time + String.valueOf(userNumber);

                    byte[] hash = {};
                    StringBuilder uid = new StringBuilder("");

                    try {
                        MessageDigest md = MessageDigest.getInstance("SHA-1");

                        hash = md.digest(tmpId.getBytes(StandardCharsets.UTF_8));

                        uid.append(UUID.nameUUIDFromBytes(hash).toString());

                        // Add userInfos table entry to the database for the anonymous user
                        Map<String, Object> userInfoMap = new HashMap<>();
                        userInfoMap.put("first_name", "");
                        userInfoMap.put("last_name", "");
                        userInfoMap.put("address", "");
                        userInfoMap.put("city", "");
                        userInfoMap.put("post_code", "");
                        userInfoMap.put("score", 0);
                        userInfoMap.put("score_time", UserInfoEntry.scoreTimeFormat.format(
                            UserInfoEntry.getDayBeforeDate(date)));

                        UserInfoEntry userInfo = new UserInfoEntry(mDatabase, uid.toString(), userInfoMap);
                        userInfo.createDBFields(new TaskCompletionManager() {
                            @Override
                            public void onSuccess() {

                                String uidText = uid.toString();

                                Log.d("BeautyAndroid", "The user automatic identifier was created: "
                                    + uidText);

                                // Store the created anonymous id to the app preferences
                                Log.v("BeautyAndroid", "Anonymous uid stored to the app preferences: "
                                    + uidText);
                                mSharedPref.edit().putString(getString(R.string.anonymous_uid), uidText)
                                    .commit();

                                startAppWithUser(mSharedPref, R.id.action_HomeFragment_to_AppFragment, uidText,
                                    AppUser.AuthenticationType.NOT_REGISTERED);
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

    private String getPreferenceIds() {
        final Context ctxt = getContext();

        if (ctxt == null) {
            Log.w("BeautyAndroid", "No context to get the app preferences");
            return "";
        }

        // Read the app preferences
        mSharedPref = ctxt.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

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
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // From Android 10
                mDeviceId.append(Settings.Secure.getString(
                    ctxt.getContentResolver(),
                    Settings.Secure.ANDROID_ID));
            } else {
                TelephonyManager telephonyManager = (TelephonyManager) ctxt.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager.getDeviceId() != null) {
                    mDeviceId.append(telephonyManager.getDeviceId());
                } else {
                    mDeviceId.append(Settings.Secure.getString(
                        ctxt.getContentResolver(),
                        Settings.Secure.ANDROID_ID));
                }
            }

            if (mDeviceId.toString().equals("")) {
                Log.e("BeautyAndroid", "Cannot determine the device id");
            } else {
                mSharedPref.edit().putString(getString(R.string.device_id), mDeviceId.toString()).commit();
            }
        }

        return "";
    }
}