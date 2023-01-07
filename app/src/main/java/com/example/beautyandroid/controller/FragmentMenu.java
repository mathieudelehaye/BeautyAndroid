//
//  FragmentMenu.java
//
//  Created by Mathieu Delehaye on 28/12/2022.
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

package com.beautyorder.androidclient.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.databinding.FragmentMenuBinding;
import com.beautyorder.androidclient.model.AppUser;

public class FragmentMenu extends Fragment {

    private FragmentMenuBinding binding;
    private SharedPreferences mSharedPref;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        mSharedPref = getContext().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        binding = FragmentMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.logOutMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Delete the app preferences, app user object and navigate to the Home page
                mSharedPref.edit().putString(getString(R.string.app_uid), "").commit();

                AppUser.getInstance().authenticate("", AppUser.AuthenticationType.NONE);

                NavHostFragment.findNavController(FragmentMenu.this)
                    .navigate(R.id.action_AppFragment_to_HomeFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}