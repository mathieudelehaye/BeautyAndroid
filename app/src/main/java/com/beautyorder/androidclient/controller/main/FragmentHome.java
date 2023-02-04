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

package com.beautyorder.androidclient.controller.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.beautyorder.androidclient.databinding.FragmentHomeBinding;
import com.google.firebase.firestore.FirebaseFirestore;

public class FragmentHome extends Fragment {

    private FragmentHomeBinding mBinding;
    private FirebaseFirestore mDatabase;
    private StringBuilder mPrefUserId;
    private StringBuilder mDeviceId;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*mDatabase = FirebaseFirestore.getInstance();

        // Navigate to the App screen if there is a registered uid in the app preferences
        getPreferenceIds();
        String lastUId = mPrefUserId.toString();
        if (!lastUId.equals("") && Helpers.isEmail(lastUId)) {
            startAppWithUser(R.id.action_HomeFragment_to_AppFragment, lastUId, AppUser.AuthenticationType.REGISTERED);
        }

        mBinding.noChoiceHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String anonymousUid = getAnonymousUidFromPreferences();
                if (!anonymousUid.equals("")) {
                    // Reuse the anonymous uid if it already exists in the app preferences
                    Log.v("BeautyAndroid", "Anonymous uid reused: " + anonymousUid);

                    startAppWithUser(R.id.action_HomeFragment_to_AppFragment, anonymousUid,
                        AppUser.AuthenticationType.NOT_REGISTERED);
                } else {
                    searchDBForAutoUserId();
                }
            }
        });

        mBinding.choice1Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FragmentHome.this)
                    .navigate(R.id.action_HomeFragment_to_LoginFragment);
            }
        });

        mBinding.choice2Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FragmentHome.this)
                    .navigate(R.id.action_HomeFragment_to_RegisterFragment);
            }
        });*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}