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

package com.beautyorder.androidclient.controller.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.beautyorder.androidclient.Helpers;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.controller.auth.AuthenticateActivity;
import com.beautyorder.androidclient.databinding.FragmentMenuBinding;
import com.beautyorder.androidclient.model.AppUser;

public class FragmentMenu extends Fragment {

    private FragmentMenuBinding mBinding;
    private SharedPreferences mSharedPref;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        mSharedPref = getContext().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        mBinding = FragmentMenuBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.v("BeautyAndroid", "Menu view created at timestamp: "
            + Helpers.getTimestamp());

        super.onViewCreated(view, savedInstanceState);

        switchLogoutButtonVisibility();

        mBinding.helpMenu.setOnClickListener(view1 -> {
            var activity = (MainActivity)getActivity();

            activity.navigate(MainActivity.FragmentType.HELP);
        });

        mBinding.termsMenu.setOnClickListener(view12 -> {
            var activity = (MainActivity)getActivity();
            activity.navigate(MainActivity.FragmentType.TERMS);
        });

        mBinding.logOutMenu.setOnClickListener(view13 -> {

            // Delete the app preferences, app user object and navigate to the Home page
            mSharedPref.edit().putString(getString(R.string.app_uid), "").commit();

            AppUser.getInstance().authenticate("", AppUser.AuthenticationType.NONE);

            // Display the first page with the result list at next startup
            CollectionPagerAdapter.setPage(0);

            startActivity(new Intent(getContext(), AuthenticateActivity.class));
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.d("BeautyAndroid", "Menu view becomes visible");

            CollectionPagerAdapter.setPage(2);

            switchLogoutButtonVisibility();

            var activity = (MainActivity)getActivity();
            if ((activity) != null) {
                activity.enableTabSwiping();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void switchLogoutButtonVisibility() {

        final var fragmentRootView = getView();
        if (fragmentRootView == null) {
            return;
        }

        Button logoutButton = fragmentRootView.findViewById(R.id.log_out_menu);
        if (logoutButton == null) {
            return;
        }

        // Show the logout button if the uid is a registered one. Hide the button otherwise
        switch (AppUser.getInstance().getAuthenticationType()) {
            case REGISTERED:
                logoutButton.setVisibility(View.VISIBLE);
                break;
            default:
                logoutButton.setVisibility(View.GONE);
                break;
        }
    }
}