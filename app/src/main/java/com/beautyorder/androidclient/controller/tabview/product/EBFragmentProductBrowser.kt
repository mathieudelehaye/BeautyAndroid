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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.java.androidjavatools.controller.tabview.product.FragmentProductBrowser
import com.beautyorder.androidclient.R

class EBFragmentProductBrowser: FragmentProductBrowser() {
    @Composable
    override fun viewContent() {
        browserView()
    }

    @Composable
    fun browserView(
    ) {
        val images = intArrayOf(R.drawable.beauty01, R.drawable.beauty02, R.drawable.beauty03,
            R.drawable.beauty04, R.drawable.beauty05)

        Column {
            Spacer(modifier = Modifier.height(105.dp))
            Row {
                Spacer(modifier = Modifier.width(15.dp))
                browserButton("Free Samples", Color(0xFF3FA3BD))    // Light blue
                Spacer(modifier = Modifier.width(5.dp))
                browserButton("Free Products", Color(0xFFD0A038))     // Orange)
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

    @Composable
    fun browserButton(
        title: String
        , color: Color
    ) {
        Button(
            modifier = Modifier
                .width(width = 188.dp)
                .height(height = 60.dp)
            , shape = RoundedCornerShape(size = 15.dp)
            , border = BorderStroke(
                1.dp
                , Color.Black
            )
            , onClick = {
            }
            , colors = ButtonDefaults.buttonColors(
                backgroundColor = color
            )
        ) {
            Text(
                text = title
                , fontWeight = FontWeight.W400
                , fontSize = 22.sp
                , textAlign = TextAlign.Center
            )
        }
    }

    @Preview
    @Composable
    fun previewBrowserButton() {
        browserButton("Free Samples", Color(0xFF3FA3BD))
    }

    @Composable
    fun browserPager(
        title: String
        , images: IntArray)
    {
        Column {
            Spacer(modifier = Modifier.height(5.dp))
            Row {
                Spacer(modifier = Modifier.width(25.dp))
                Text(
                    text = title
                    , fontSize = 20.sp
                    , fontWeight = FontWeight.Bold
                    , modifier = Modifier
                        .padding(all = 4.dp)
                    , style = MaterialTheme.typography.h1
                )
            }
            infinitePager(images)
            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color.LightGray, thickness = 2.dp)
        }
    }

    @Preview
    @Composable
    fun previewBrowserPager() {
        val images = intArrayOf(R.drawable.beauty01, R.drawable.beauty02, R.drawable.beauty03,
            R.drawable.beauty04, R.drawable.beauty05)
        browserPager("Browse by Functions", images)
    }
}