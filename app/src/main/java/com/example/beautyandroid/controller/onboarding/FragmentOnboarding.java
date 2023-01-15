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

package com.example.beautyandroid.controller.onboarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.leanback.app.OnboardingSupportFragment;

public class FragmentOnboarding extends OnboardingSupportFragment {

    @Override
    protected int getPageCount() {
        return 0;
    }

    @Override
    protected CharSequence getPageTitle(int pageIndex) {
        return null;
    }

    @Override
    protected CharSequence getPageDescription(int pageIndex) {
        return null;
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
        return null;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    protected View onCreateForegroundView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }
}
