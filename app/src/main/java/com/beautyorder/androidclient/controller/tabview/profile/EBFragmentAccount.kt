//
//  EBFragmentAccount.java
//
//  Created by Mathieu Delehaye on 21/05/2023.
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

package com.beautyorder.androidclient.controller.tabview.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.android.java.androidjavatools.controller.tabview.profile.FragmentAccount
import com.beautyorder.androidclient.R
import com.beautyorder.androidclient.controller.auth.AuthenticateActivity
import com.beautyorder.androidclient.controller.tabview.EBCollectionPagerAdapter

class EBFragmentAccount: FragmentAccount() {
    override fun onLogout() {
        // Delete the app current user
        val sharedPref: SharedPreferences = (mActivity!! as Context).getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE)
        sharedPref.edit().putString(getString(R.string.app_uid), "").commit()

        // Display the first page with the result list at next startup
        EBCollectionPagerAdapter.setPage(0);

        // Start the auth activity
        startActivity(Intent(context, AuthenticateActivity::class.java))
    }
}