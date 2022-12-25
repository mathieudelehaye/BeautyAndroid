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

package com.beautyorder.androidclient;

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
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.beautyorder.androidclient.databinding.FragmentHomeBinding;
import com.example.beautyandroid.model.AppUser;
import com.example.beautyandroid.model.UserInfoEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import org.checkerframework.checker.units.qual.A;
import org.osmdroid.views.overlay.OverlayItem;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class FragmentHome extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore mDatabase;
    private SharedPreferences mSharedPref;
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

        binding.noChoiceHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String autoUserId = getAutoUserId();

                if (!autoUserId.equals(""))
                {
                    AppUser.getInstance().authenticate(autoUserId, AppUser.AuthenticationType.NOT_REGISTERED);

                    NavHostFragment.findNavController(FragmentHome.this)
                        .navigate(R.id.action_HomeFragment_to_AppFragment);
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

                        // Add userInfos table entry to the database matching the anonymous user
                        Map<String, Object> userInfoMap = new HashMap<>();
                        userInfoMap.put("first_name", "");
                        userInfoMap.put("last_name", "");
                        userInfoMap.put("address", "");
                        userInfoMap.put("city", "");
                        userInfoMap.put("post_code", "");
                        userInfoMap.put("score", 0);

                        UserInfoEntry userInfo = new UserInfoEntry(mDatabase, uid.toString(), userInfoMap);
                        userInfo.writeToDatabase(new UserInfoEntry.CallbackManager() {
                            @Override
                            public void onSuccess() {
                                // Store the uid in the app preferences
                                SharedPreferences.Editor editor = mSharedPref.edit();
                                editor.putString(getString(R.string.user_automatic_id), uid.toString());
                                editor.apply();

                                Log.d("BeautyAndroid", "The user automatic identifier was created: "
                                    + uid);

                                // Update the app user
                                AppUser.getInstance().authenticate(uid.toString(), AppUser.AuthenticationType.NOT_REGISTERED);

                                NavHostFragment.findNavController(FragmentHome.this)
                                    .navigate(R.id.action_HomeFragment_to_AppFragment);
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

    private String getAutoUserId() {
        final Context ctxt = getContext();

        if (ctxt == null) {
            Log.w("BeautyAndroid", "Trying to get the id without application user");
            return "";
        }

        // Read the app preferences
        mSharedPref = ctxt.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        StringBuilder userAutomaticId = new StringBuilder();
        userAutomaticId.append(mSharedPref.getString(getString(R.string.user_automatic_id), ""));

        if (!userAutomaticId.toString().equals("")) {
            Log.d("BeautyAndroid", "The user automatic identifier was read: " + userAutomaticId.toString());
            return userAutomaticId.toString();
        }

        // Get the phone id
        mDeviceId = new StringBuilder("");

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

        tryAndCreateAutoUserId();

        return "";
    }
}