//
//  EBFragmentProductDetail.kt
//
//  Created by Mathieu Delehaye on 25/05/2023.
//
//  AndroidJavaTools: A framework to develop Android apps in Java.
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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.java.androidjavatools.controller.tabview.product.FragmentProductDetail
import com.beautyorder.androidclient.R

class EBFragmentProductDetail :
    FragmentProductDetail() {

    @Composable
    override fun productDescription() {

        Column (modifier = Modifier
            .background(Color.White)
            .fillMaxWidth()
        ) {
            iconRow()
            Divider(color = Color.LightGray, thickness = 2.dp)
            Column(
                horizontalAlignment = Alignment.Start
                , modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Bla bla bla"
                    , fontWeight = FontWeight.W600
                    , fontSize = 20.sp
                    , textAlign = TextAlign.Center
                    , color = Color.Blue
                )
                Text(
                    text = "Bla bla blu"
                    , fontWeight = FontWeight.W500
                    , fontSize = 18.sp
                    , textAlign = TextAlign.Center
                    , maxLines = 2
                    , overflow = TextOverflow.Visible
                    , softWrap = true
                    , color = Color.Black
                    , modifier = Modifier
                    .height(60.dp)
                )
            }
        }
    }

    @Composable
    fun iconRow() {
        val iconSize = 60.dp

        Row {
            Spacer(modifier = Modifier.width(iconSize))
            Image(
                painter = painterResource(id = R.drawable.antiaging)
                , contentDescription = "Image anti-aging"
                , contentScale = ContentScale.Fit
                , modifier = Modifier
            .size(iconSize)
            )
            Image(
                painter = painterResource(id = R.drawable.acne)
                , contentDescription = "Image acne"
                , contentScale = ContentScale.Fit
                , modifier = Modifier
                    .size(iconSize)
            )
            Image(
                painter = painterResource(id = R.drawable.sensitive)
                , contentDescription = "Image sensitive"
                , contentScale = ContentScale.Fit
                , modifier = Modifier
                    .size(iconSize)
            )
            Image(
                painter = painterResource(id = R.drawable.t_zone)
                , contentDescription = "Image t-zone"
                , contentScale = ContentScale.Fit
                , modifier = Modifier
                    .size(iconSize)
            )
            Image(
                painter = painterResource(id = R.drawable.haircomb)
                , contentDescription = "Image haircomb"
                , contentScale = ContentScale.Fit
                , modifier = Modifier
                    .size(iconSize)
            )
        }
    }

    @Preview
    @Composable
    fun productDescriptionPreview() {
        productDescription()
    }
}