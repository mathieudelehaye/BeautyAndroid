//
//  MainActNavigatorivity.java
//
//  Created by Mathieu Delehaye on 27/02/2023.
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

package com.beautyorder.androidclient.controller;

import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.beautyorder.androidclient.R;

public class Navigator {
    private Fragment mShownFragment;
    private Fragment mPrevFragment;
    private Activity mActivity;

    public Navigator(Activity activity) {
        mActivity = activity;
    }

    public void showFragment(Fragment fragment) {

        if (mShownFragment != null) {
            hideFragment(mShownFragment);
        }

        ((AppCompatActivity)mActivity).getSupportFragmentManager().beginTransaction()
            .show(fragment)
            .commit();

        mPrevFragment = mShownFragment;
        mShownFragment = fragment;

        fragment.setUserVisibleHint(true);
    }

    public void back() {
        showFragment(mPrevFragment);
    }

    public void addFragment(Fragment fragment) {

        ((AppCompatActivity)mActivity).getSupportFragmentManager().beginTransaction()
            .add(R.id.mainActivityLayout, fragment)
            .hide(fragment)
            .commit();
    }

    private void hideFragment(Fragment fragment){
        ((AppCompatActivity)mActivity).getSupportFragmentManager().beginTransaction()
            .hide(fragment)
            .commit();

        mShownFragment = null;

        fragment.setUserVisibleHint(false);
    }
}
