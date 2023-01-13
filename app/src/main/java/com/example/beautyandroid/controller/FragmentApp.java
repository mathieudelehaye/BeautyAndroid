//
//  FragmentApp.java
//
//  Created by Mathieu Delehaye on 17/12/2022.
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

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.beautyorder.androidclient.NotSwipeableViewPager;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.databinding.FragmentAppBinding;
import com.beautyorder.androidclient.CollectionPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class FragmentApp extends Fragment {
    private FragmentAppBinding binding;
    private NotSwipeableViewPager viewPager;

    private Boolean keyboardDisplayed = false;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        binding = FragmentAppBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = view.findViewById(R.id.appPager);
        TabLayout tabLayout = view.findViewById(R.id.appTabbar);
        tabLayout.getTabAt(0).setIcon(R.drawable.home);
        tabLayout.getTabAt(1).setIcon(R.drawable.camera);
        tabLayout.setupWithViewPager(viewPager);
        CollectionPagerAdapter adapter = new CollectionPagerAdapter(getChildFragmentManager(), getActivity());
        viewPager.setAdapter(adapter);

        // Disable the swiping gesture for the view pager
        viewPager.setPagingEnabled(false);
        viewPager.beginFakeDrag();

        view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                View mapLayout = view.findViewById(R.id.mapLayout);

                Rect viewBorder = new Rect();

                // border will be populated with the coordinates of your view that area still visible
                viewPager.getWindowVisibleDisplayFrame(viewBorder);

                final int viewBorderHeight = viewBorder.height();
                final int viewPagerRootViewHeight = viewPager.getRootView().getHeight();

                final int heightDiff = viewPagerRootViewHeight - viewBorderHeight;

                if (heightDiff > 0.25*viewPagerRootViewHeight) { // if more than 25% of the screen, it's probably a keyboard
                    if (!keyboardDisplayed && mapLayout != null) {
                        keyboardDisplayed = true;

                        Log.d("BeautyAndroid", "Keyboard displayed");

                        ViewGroup.LayoutParams params = mapLayout.getLayoutParams();
                        params.height = 540;
                        mapLayout.requestLayout();
                    }
                } else {
                    if (keyboardDisplayed && mapLayout != null) {
                        keyboardDisplayed = false;

                        Log.d("BeautyAndroid", "Keyboard hidden");

                        ViewGroup.LayoutParams params = mapLayout.getLayoutParams();
                        params.height = 788;
                        mapLayout.requestLayout();
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}