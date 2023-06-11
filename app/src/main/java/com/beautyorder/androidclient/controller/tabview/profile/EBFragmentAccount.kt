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

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.java.androidjavatools.controller.tabview.profile.FragmentAccount
import com.beautyorder.androidclient.controller.tabview.EBCollectionPagerAdapter
import com.beautyorder.androidclient.controller.tabview.EBTabViewActivity

class EBFragmentAccount: FragmentAccount() {
    override fun onLogout() {
        // Display the first page with the result list at next startup
        EBCollectionPagerAdapter.setPage(0);

        // Restart the main activity
        startActivity(Intent(context, EBTabViewActivity::class.java))
    }

    @Preview
    @Composable
    fun accountContentView() {
        contentView()
    }
}