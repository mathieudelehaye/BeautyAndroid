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

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.java.androidjavatools.controller.tabview.product.FragmentProductBrowser
import com.beautyorder.androidclient.R

class EBFragmentProductBrowser: FragmentProductBrowser() {
    @Composable
    override fun viewContent() {
        val images = intArrayOf(R.drawable.beauty01, R.drawable.beauty02, R.drawable.beauty03,
            R.drawable.beauty04, R.drawable.beauty05)

        Column {
            Spacer(modifier = Modifier.height(130.dp))
            Text(
                text = "Browse by Functions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(all = 4.dp),
                style = MaterialTheme.typography.h1
            )
            infinitePager(images)
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Sustainable Brands",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(all = 4.dp),
                style = MaterialTheme.typography.h1
            )
            infinitePager(images)
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Popular on ECOBEAUTY",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(all = 4.dp),
                style = MaterialTheme.typography.h1
            )
            infinitePager(images)
        }
    }
}