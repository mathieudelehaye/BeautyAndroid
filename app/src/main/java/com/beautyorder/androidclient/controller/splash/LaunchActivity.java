//
//  LaunchActivity.java
//
//  Created by Mathieu Delehaye on 22/02/2023.
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

package com.beautyorder.androidclient.controller.splash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.controller.onboard.OnboardActivity;

public class LaunchActivity extends Activity {

    protected SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Keep the splash screen visible for this Activity
        //splashScreen.setKeepOnScreenCondition(() -> true);

        // Read the app preferences
        mSharedPref = getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        // Check if we need to display our OnboardingFragment
        if (!mSharedPref.getBoolean(
            getString(R.string.completed_onboarding), false)) {

            Log.i("BeautyAndroid", "Onboarding activity started");
            startActivity(new Intent(this, OnboardActivity.class));
        } else {
            Log.i("BeautyAndroid", "Onboarding activity skipped");
            startActivity(new Intent(this,
                com.beautyorder.androidclient.controller.auth.AuthenticateActivity.class));
        }

        finish();
    }
}
