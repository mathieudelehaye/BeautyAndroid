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
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.beautyorder.androidclient.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class FragmentHome extends Fragment {

    private FragmentHomeBinding binding;

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

        binding.noChoiceHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getAutoUserId();

                NavHostFragment.findNavController(FragmentHome.this)
                    .navigate(R.id.action_HomeFragment_to_AppFragment);
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

    private void getAutoUserId() {
        Context ctxt = getContext();

        if (ctxt == null) {
            return;
        }

        // Read the shared app preferences
        SharedPreferences sharedPref = ctxt.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        StringBuilder userAutomaticId = new StringBuilder();
        userAutomaticId.append(sharedPref.getString(getString(R.string.user_automatic_id), ""));

        if (!userAutomaticId.toString().equals("")) {
            Log.d("BeautyAndroid", "The user automatic identifier was read: " + userAutomaticId.toString());
            return;
        }

        // Get the phone uid
        StringBuilder deviceId = new StringBuilder("");

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // From Android 10
            deviceId.append(Settings.Secure.getString(
                ctxt.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) ctxt.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager.getDeviceId() != null) {
                deviceId.append(telephonyManager.getDeviceId());
            } else {
                deviceId.append(Settings.Secure.getString(
                    ctxt.getContentResolver(),
                    Settings.Secure.ANDROID_ID));
            }
        }

        // Get the timestamp
        Date date = new Date();
        String time = String.valueOf(date.getTime());

        // Query the database
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("userInfos")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    // Get the user number already in the database
                    int userNumber = 0;

                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        userNumber =  snapshot.size();
                    }

                    // Compute the user id hash
                    String tmpId = deviceId + time + String.valueOf(userNumber);

                    byte[] hash = {};
                    try {
                        MessageDigest md = MessageDigest.getInstance("SHA-1");

                        hash = md.digest(tmpId.getBytes(StandardCharsets.UTF_8));

                        // Store the hash in the app preferences
                        userAutomaticId.append(hash.toString());

                        Log.d("BeautyAndroid", "The user automatic identifier was created: " + userAutomaticId.toString());

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.user_automatic_id), hash.toString());
                        editor.apply();
                    } catch (NoSuchAlgorithmException e) {
                        Log.e("BeautyAndroid", e.toString());
                        return;
                    }
                }
            });
    }
}