//
//  EBFragmentProductDetail.kt
//
//  Created by Mathieu Delehaye on 25/05/2023.
//
//  AndroidJavaTools: A framework to develop Android apps with Java Technologies.
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

import android.util.Log
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.java.androidjavatools.controller.tabview.product.FragmentProductDetail
import com.android.java.androidjavatools.controller.template.FragmentHelpDialog
import com.android.java.androidjavatools.model.AppUser
import com.android.java.androidjavatools.model.TaskCompletionManager
import com.beautyorder.androidclient.R
import com.beautyorder.androidclient.model.EBUserInfoDBEntry

class EBFragmentProductDetail : FragmentProductDetail() {
    // TODO: confirm the cost to order a product
    val scoreCostToOrder = 1

    override val mUserInfoDBEntry = EBUserInfoDBEntry(mDatabase, AppUser.getInstance().id)

    @Composable
    override fun productDescription() {

        Column (modifier = Modifier
            .background(Color.White)
            .fillMaxWidth()
        ) {
            iconRow()
            Spacer(modifier = Modifier
                .height(5.dp)
            )
            Divider(color = Color.LightGray, thickness = 2.dp)
            Spacer(modifier = Modifier
                .height(5.dp)
            )
            Column(
                horizontalAlignment = Alignment.Start
                , modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth()
            ) {
                textSection("Description:",
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor"
                )
                Spacer(modifier = Modifier
                    .height(10.dp)
                )
                textSection("Ingredients:",
                    "Odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem"
                )
            }
        }
    }

    override fun onOrdering(productKey : String, successDialogMessage : String, onSuccessDialogClose: () -> Unit) {
        // read score and possibly update profile data
        mUserInfoDBEntry.readScoreDBFields(object : TaskCompletionManager {
            override fun onSuccess() {
                val oldScore = mUserInfoDBEntry.getScore()
                if (oldScore >= scoreCostToOrder) {
                    updateProfile(productKey, (oldScore - scoreCostToOrder), successDialogMessage,
                        onSuccessDialogClose)
                } else {
                    FragmentHelpDialog("Your score must be at least $scoreCostToOrder to order samples!")
                        .show(childFragmentManager, "Order refused dialog")
                }
            }

            override fun onFailure() {}
        })
    }

    private fun updateProfile(productKey : String, newScore : Int, successDialogMessage : String,
        onSuccessDialogClose: () -> Unit) {

        mUserInfoDBEntry.setOrderedSampleKey(productKey)
        mUserInfoDBEntry.setScore(newScore)
        mUserInfoDBEntry.updateDBFields(object : TaskCompletionManager {
            override fun onSuccess() {
                Log.i("EBT", "Ordering of product $productKey written to the DB for user " +
                    "${mUserInfoDBEntry.key}. User score changed to $newScore")

                FragmentHelpDialog("$successDialogMessage. Your score is now $newScore!") {
                    mNavigatorManager?.navigator()?.back()
                }.show(childFragmentManager, "Order approved dialog")
            }

            override fun onFailure() {
                Log.e("EBT", "Error while writing order for user ${mUserInfoDBEntry.key}")
            }
        })
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

    @Composable
    fun textSection(title: String, content: String) {
        val textHorPadding = 15.dp

        Text(
            text = title
            , fontWeight = FontWeight.W400
            , fontSize = 16.sp
            , textAlign = TextAlign.Start
            , color = Color.Black
            , modifier = Modifier
                .padding(start = textHorPadding)
        )
        Text(
            text = content
            , fontWeight = FontWeight.W300
            , fontSize = 14.sp
            , textAlign = TextAlign.Start
            , color = Color.Black
            , modifier = Modifier
                .padding(start = textHorPadding)
        )
    }

    @Preview
    @Composable
    fun productDescriptionPreview() {
        productDescription()
    }
}