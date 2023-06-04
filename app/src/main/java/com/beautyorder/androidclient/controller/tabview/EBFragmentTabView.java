//
//  EBFragmentTabView.java
//
//  Created by Mathieu Delehaye on 25/04/2023.
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

package com.beautyorder.androidclient.controller.tabview;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.android.java.androidjavatools.controller.tabview.FragmentTabView;
import com.android.java.androidjavatools.controller.template.ResultProvider;

public class EBFragmentTabView extends FragmentTabView {
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewPager.setAdapter(new EBCollectionPagerAdapter(getChildFragmentManager(), getActivity(),
            (ResultProvider) getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();

        final int pageToDisplay = EBCollectionPagerAdapter.getPage();
        mViewPager.setCurrentItem(pageToDisplay);
    }
}
