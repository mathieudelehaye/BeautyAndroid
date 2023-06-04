//
//  EBCollectionPagerAdapter.java
//
//  Created by Mathieu Delehaye on 19/12/2022.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
//
//  Copyright © 2022 Mathieu Delehaye. All rights reserved.
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

package com.beautyorder.androidclient.controller.tabview;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import com.android.java.androidjavatools.controller.tabview.CollectionPagerAdapter;
import com.android.java.androidjavatools.controller.template.ResultProvider;
import com.beautyorder.androidclient.controller.tabview.camera.EBFragmentCamera;
import com.beautyorder.androidclient.controller.tabview.home.EBFragmentHome;
import com.beautyorder.androidclient.controller.tabview.profile.EBFragmentProfileMenu;
import com.beautyorder.androidclient.controller.tabview.saved.EBFragmentSavedList;

public class EBCollectionPagerAdapter extends CollectionPagerAdapter {
    public EBCollectionPagerAdapter(FragmentManager fm, FragmentActivity fa, ResultProvider resultProvider) {
        super(fm, fa, resultProvider);
    }

    @Override
    protected Fragment findTabFragment(int i) {
        Fragment fragment;

        switch (i) {
            case 0:
                fragment = new EBFragmentHome(mSearchResultProvider);
                break;
            case 1:
                fragment = new EBFragmentSavedList(mSearchResultProvider);
                break;
            case 2:
                fragment = new EBFragmentCamera();
                break;
            case 3:
            default:
                fragment = new EBFragmentProfileMenu();
                break;
        }

        return fragment;
    }
}