//
//  EBFragmentProductBrowser.java
//
//  Created by Mathieu Delehaye on 7/05/2023.
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

package com.beautyorder.androidclient.controller.tabview.product

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.java.androidjavatools.controller.tabview.product.FragmentProductBrowser
import com.android.java.androidjavatools.controller.tabview.search.SearchBox
import com.beautyorder.androidclient.R

class EBFragmentProductBrowser: FragmentProductBrowser() {
    @Composable
    override fun viewContent() {
        browserView()
    }

    override fun searchAndDisplayItems() {
        TODO("Not yet implemented")
    }

    @Composable
    fun browserView(
    ) {
        var searchBox = SearchBox(this.activity as Activity, this, null)

        val images = intArrayOf(R.drawable.beauty01, R.drawable.beauty02, R.drawable.beauty03,
            R.drawable.beauty04, R.drawable.beauty05)

        Column {
            Spacer(modifier = Modifier.height(56.dp))
            searchBox.show()

            Spacer(modifier = Modifier.height(45.dp))
            Row {
                Spacer(modifier = Modifier.width(15.dp))
                browserButton("Free Samples", Color(0xFF3FA3BD))    // Light blue
                Spacer(modifier = Modifier.width(5.dp))
                browserButton("Free Products", Color(0xFFD0A038))     // Orange
            }

            Spacer(modifier = Modifier.height(5.dp))
            browserPager("Browse by Functions", images)
            browserPager("Sustainable Brands", images)
            browserPager("Popular on ECOBEAUTY", images)
        }
    }

    @Preview
    @Composable
    fun previewBrowserView() {
        browserView()
    }
}