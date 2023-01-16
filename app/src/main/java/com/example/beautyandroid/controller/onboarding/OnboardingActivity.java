//
//  OnboardingActivity.java
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

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.beautyorder.androidclient.controller.onboarding.FragmentOnboarding;
import com.beautyorder.androidclient.R;

public class OnboardingActivity extends AppCompatActivity {

    public OnboardingActivity() {
        super(R.layout.activity_onboarding);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_container_view, FragmentOnboarding.class, null)
                .commit();
        }
    }
}
