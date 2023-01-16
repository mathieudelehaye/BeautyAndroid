//
//  FragmentOnboarding.java
//
//  Created by Mathieu Delehaye on 14/01/2023.
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

package com.beautyorder.androidclient.controller.onboarding;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.leanback.app.OnboardingSupportFragment;
import com.beautyorder.androidclient.R;

public class FragmentOnboarding extends OnboardingSupportFragment {

    private SharedPreferences mSharedPref;

    @Override
    protected int getPageCount() {
        return 3;
    }

    @Override
    protected CharSequence getPageTitle(int pageIndex) {

        CharSequence pageTitle = "";

        switch (pageIndex) {
            case 0:
                pageTitle = "Page 1";
                break;
            case 1:
                pageTitle = "Page 2";
                break;
            case 2:
            default:
                pageTitle = "Page 3";
                break;
        }

        return pageTitle;
    }

    @Override
    protected CharSequence getPageDescription(int pageIndex) {

        CharSequence pageDescr = "";

        switch (pageIndex) {
            case 0:
                pageDescr = "This is the description of page 1";
                break;
            case 1:
                pageDescr = "This is the description of page 2";
                break;
            case 2:
            default:
                pageDescr = "This is the description of page 3";
                break;
        }

        return pageDescr;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    protected View onCreateBackgroundView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, ViewGroup container) {
        var contentView = new ImageView(getContext());
        contentView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        contentView.setImageResource(R.drawable.app_screenshot);
        contentView.setPadding(0, 32, 0, 32);
        return contentView;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    protected View onCreateForegroundView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Override
    public int onProvideTheme() {
        return androidx.leanback.R.style.Theme_Leanback_Onboarding;
    }

    @Override
    protected void onFinishFragment() {
        super.onFinishFragment();

        mSharedPref = getContext().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);
        var editor = mSharedPref.edit();
        editor.putBoolean(
            getString(R.string.completed_onboarding), true).apply();
    }
}
