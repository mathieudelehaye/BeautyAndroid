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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.leanback.app.OnboardingSupportFragment;
import com.beautyorder.androidclient.R;
import java.util.ArrayList;

public class FragmentOnboarding extends OnboardingSupportFragment {

    private SharedPreferences mSharedPref;
    private ImageView mContentImage;
    private ArrayList<Integer> mPageImages = new ArrayList<Integer>();

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Slide pictures
        mPageImages.add(R.drawable.onboarding_picture_01);
        mPageImages.add(R.drawable.onboarding_picture_02);
        mPageImages.add(R.drawable.onboarding_picture_03);
    }

    @Override
    protected int getPageCount() {
        return 3;
    }

    @Override
    protected CharSequence getPageTitle(int pageIndex) {

        CharSequence pageTitle = "";

        switch (pageIndex) {
            case 0:
                pageTitle = "Step 1";
                break;
            case 1:
                pageTitle = "Step 2";
                break;
            case 2:
            default:
                pageTitle = "Step 3";
                break;
        }

        return pageTitle;
    }

    @Override
    protected CharSequence getPageDescription(int pageIndex) {

        CharSequence pageDescr = "";

        switch (pageIndex) {
            case 0:
                pageDescr = "Use the map and search the directions for drop-off locations";
                break;
            case 1:
                pageDescr = "When at a location, drop off your beauty and scan the QR code";
                break;
            case 2:
            default:
                pageDescr = "Each day you drop a package earns you an EcoBeauty point! In the future, those points"
                    + " will let you order free samples and so on from this app";
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
        mContentImage = new ImageView(getContext());
        mContentImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mContentImage.setImageResource(mPageImages.get(0));
        mContentImage.setPadding(0, 32, 0, 32);
        return mContentImage;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    protected View onCreateForegroundView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Override
    protected Animator onCreateEnterAnimation() {
        var startAnimator = ObjectAnimator.ofFloat(getView(),
            View.SCALE_X, 0.2f, 1.0f).setDuration(300);
        return startAnimator;
    }

    @Override
    protected void onPageChanged(final int newPage, int previousPage) {
        // Create a fade-out animation used to fade out previousPage and, once
        // done, swaps the contentView image with the next page's image.
        Animator fadeOut = ObjectAnimator.ofFloat(getView(),
            View.ALPHA, 1.0f, 0.0f).setDuration(200);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mContentImage.setImageResource(mPageImages.get(newPage));
            }
        });
        // Create a fade-in animation used to fade in nextPage
        Animator fadeIn = ObjectAnimator.ofFloat(getView(),
            View.ALPHA, 0.0f, 1.0f).setDuration(200);
        // Create AnimatorSet with our fade-out and fade-in animators, and start it
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(fadeOut, fadeIn);
        set.start();
    }

    @Override
    public int onProvideTheme() {
        return androidx.leanback.R.style.Theme_Leanback_Onboarding;
    }

    @Override
    protected void onFinishFragment() {
        super.onFinishFragment();

        Log.d("BeautyAndroid", "Onboarding finished");

        mSharedPref = getContext().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);
        var editor = mSharedPref.edit();
        editor.putBoolean(
            getString(R.string.completed_onboarding), true).apply();

        startActivity(new Intent(getContext(), com.beautyorder.androidclient.controller.main.MainActivity.class));
    }
}
