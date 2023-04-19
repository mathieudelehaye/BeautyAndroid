//
//  FragmentTabView.java
//
//  Created by Mathieu Delehaye on 17/12/2022.
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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.android.java.androidjavatools.Helpers;
import com.android.java.androidjavatools.controller.tabview.NotSwipeableViewPager;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.controller.tabview.home.FragmentHome;
import com.beautyorder.androidclient.databinding.FragmentTabViewBinding;
import com.google.android.material.tabs.TabLayout;

public class FragmentTabView extends Fragment {
    private FragmentTabViewBinding mBinding;
    private Activity mActivity;
    private NotSwipeableViewPager mViewPager;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        mBinding = FragmentTabViewBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.v("BeautyAndroid", "App view created at timestamp: "
            + Helpers.getTimestamp());

        super.onViewCreated(view, savedInstanceState);

        mActivity = getActivity();

        mViewPager = view.findViewById(R.id.tabViewPager);
        TabLayout tabLayout = view.findViewById(R.id.tabViewTabbar);
        tabLayout.getTabAt(0).setIcon(R.drawable.home);
        tabLayout.getTabAt(1).setIcon(R.drawable.camera);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.setAdapter(new CollectionPagerAdapter(getChildFragmentManager(), getActivity()));
        mViewPager.setOffscreenPageLimit(3);    // display up to 3 pages without recreating them at each swipe
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onResume() {
        Log.v("BeautyAndroid", "Tab view fragment resumed");

        super.onResume();

        final int pageToDisplay = CollectionPagerAdapter.getPage();

        mViewPager.setCurrentItem(pageToDisplay);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void enableTabSwiping() {
        // Enable swiping gesture for the view pager
        if (mViewPager.isFakeDragging()) {
            Log.v("BeautyAndroid", "Tab swiping enabled from the current page on");
            mViewPager.setSwipingEnabled(true);
            mViewPager.endFakeDrag();
        }
    }

    public void disableTabSwiping() {
        // Disable the swiping gesture for the view pager
        if (!mViewPager.isFakeDragging()) {
            Log.v("BeautyAndroid", "Tab swiping disabled from the current page on");
            mViewPager.setSwipingEnabled(false);
            mViewPager.beginFakeDrag();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.d("BeautyAndroid", "Tab view becomes visible");

            if(mActivity != null) {
                ((FragmentHome)FragmentManager.findFragment(mActivity.findViewById(R.id.rp_history_title)))
                    .updateRecentRP();
                ((FragmentHome)FragmentManager.findFragment(mActivity.findViewById(R.id.rp_history_title)))
                    .updateRecentSearches();
            }
        }
    }
}