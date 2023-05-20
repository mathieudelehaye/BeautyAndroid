//
//  EBFragmentTerms.java
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

package com.beautyorder.androidclient.controller.tabview.profile;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.android.java.androidjavatools.controller.tabview.profile.FragmentTerms;
import com.beautyorder.androidclient.controller.tabview.TabViewActivity;

public class EBFragmentTerms extends FragmentTerms {
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.descriptionTerms.setText("This is a customized terms content.");

        mBinding.backTerms.setOnClickListener(view1 -> {
            // Go back to the app Menu
            var activity = (TabViewActivity)getActivity();
            activity.navigator().back();
        });
    }
}
