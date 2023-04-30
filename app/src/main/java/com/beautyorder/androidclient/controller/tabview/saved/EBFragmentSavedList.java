//
//  EBFragmentSavedList.java
//
//  Created by Mathieu Delehaye on 25/03/2023.
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

package com.beautyorder.androidclient.controller.tabview.saved;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import com.android.java.androidjavatools.controller.tabview.saved.FragmentSavedList;
import com.beautyorder.androidclient.controller.tabview.TabViewActivity;
import com.beautyorder.androidclient.R;

public class EBFragmentSavedList extends FragmentSavedList {
    private TabViewActivity mActivity;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mActivity = (TabViewActivity)getActivity();
        setToolbarBackgroundColor(R.color.white);
    }

    // TODO: move this method logic to the library parent class
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (mActivity == null) {
            return;
        }

        Log.d("AndroidJavaTools", "Saved view becomes " + (isVisibleToUser ? "visible" : "invisible"));
        Log.v("AndroidJavaTools", (isVisibleToUser ? "Hiding" : "Showing") + " the app regular toolbar");

        mActivity.toggleToolbar(!isVisibleToUser);

        super.setUserVisibleHint(isVisibleToUser);
    }
}
