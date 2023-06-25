//
//  EBLaunchActivity.kt
//
//  Created by Mathieu Delehaye on 25/05/2023.
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
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
//  implied warranty of MERCHANTABILITY or FITNESS
//  FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this program. If not, see
//  <https://www.gnu.org/licenses/>.

package com.beautyorder.androidclient.controller.splash

import android.content.Intent
import android.util.Log
import com.android.java.androidjavatools.controller.onboard.LaunchActivity
import com.beautyorder.androidclient.R
import com.beautyorder.androidclient.controller.onboard.OnboardActivity
import com.beautyorder.androidclient.controller.tabview.EBTabViewActivity

class EBLaunchActivity : LaunchActivity() {
    override fun startNextActivity() {
        // Read the app preferences
        mSharedPref = getSharedPreferences(
            getString(R.string.app_name), android.content.Context.MODE_PRIVATE)

        // Check if we need to display our OnboardingFragment
        if (!mSharedPref.getBoolean(
            getString(R.string.completed_onboarding), false))
        {
            Log.i("EBT", "Onboarding activity started")
            startActivity(Intent(this, OnboardActivity::class.java))
        } else
        {
            Log.i("EBT", "Onboarding activity skipped")
            startActivity(Intent(this, EBTabViewActivity::class.java))
        }
    }
}
