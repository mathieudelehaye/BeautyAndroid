//
//  EBFragmentMenu.java
//
//  Created by Mathieu Delehaye on 20/04/2023.
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

package com.beautyorder.androidclient.controller.tabview.menu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.android.java.androidjavatools.controller.tabview.menu.FragmentMenu;
import com.android.java.androidjavatools.model.AppUser;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.controller.auth.AuthenticateActivity;
import com.beautyorder.androidclient.controller.tabview.CollectionPagerAdapter;
import com.beautyorder.androidclient.controller.tabview.TabViewActivity;

public class EBFragmentMenu extends FragmentMenu {
    private SharedPreferences mSharedPref;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        mSharedPref = getContext().getSharedPreferences(
        getString(R.string.app_name), Context.MODE_PRIVATE);

        return  rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.helpMenu.setOnClickListener(view1 -> {
            var activity = (TabViewActivity)getActivity();
            activity.navigate(TabViewActivity.FragmentType.HELP);
        });

        mBinding.termsMenu.setOnClickListener(view12 -> {
            var activity = (TabViewActivity)getActivity();
            activity.navigate(TabViewActivity.FragmentType.TERMS);
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
            CollectionPagerAdapter.setPage(2);

            var activity = (TabViewActivity)getActivity();
            if ((activity) != null) {
                activity.toggleTabSwiping(true);
            }
        }
    }
}
