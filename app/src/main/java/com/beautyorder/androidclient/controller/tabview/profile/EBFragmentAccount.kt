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
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.android.java.androidjavatools.R
import com.android.java.androidjavatools.controller.tabview.profile.FragmentAccount
import com.android.java.androidjavatools.controller.template.Navigator
import com.android.java.androidjavatools.model.user.AppUser
import com.android.java.androidjavatools.model.TaskCompletionManager
import com.beautyorder.androidclient.controller.tabview.EBCollectionPagerAdapter
import com.beautyorder.androidclient.controller.tabview.EBTabViewActivity
import com.beautyorder.androidclient.databinding.FragmentEbAccountBinding
import com.beautyorder.androidclient.model.EBUserInfoDBEntry

class EBFragmentAccount: FragmentAccount() {
    override val mUserInfoDBEntry = EBUserInfoDBEntry(mDatabase, AppUser.getInstance().id)
    private var mUserScore: MutableState<Int> = mutableStateOf(0)

    // TODO: improve reusability and avoid overriding `contentView`
    @Composable
    override fun contentView() {
        val mNavigatorManager : Navigator.NavigatorManager = mActivity!! as Navigator.NavigatorManager

        var firstName by remember { mUserFirstName }
        var lastName by remember { mUserLastName }
        var address by remember { mUserAddress }
        var city by remember { mUserCity }
        var postCode by remember { mUserPostcode }
        var email by remember { mUserEmail }
        var score by remember { mUserScore }

        AndroidViewBinding(
            factory = FragmentEbAccountBinding::inflate,
            modifier = Modifier
        ) {
            accountFirstName.setText(firstName)
            accountLastName.setText(lastName)
            accountAddress.setText(address)
            accountCity.setText(city)
            accountPostCode.setText(postCode)
            accountEmail.setText(email)
            accountScoreValue.text = "$score EB"

            accountConfirm.setOnClickListener {
                // TODO: avoid repeating instructions and call a method instead
                if (mUserInfoDBEntry.firstName != accountFirstName.text.toString()) {
                    firstName = accountFirstName.text.toString()
                    mUserInfoDBEntry.firstName = firstName
                }

                if (mUserInfoDBEntry.lastName != accountLastName.text.toString()) {
                    lastName = accountLastName.text.toString()
                    mUserInfoDBEntry.lastName = lastName
                }

                if (mUserInfoDBEntry.address != accountAddress.text.toString()) {
                    address = accountAddress.text.toString()
                    mUserInfoDBEntry.address = address
                }

                if (mUserInfoDBEntry.city != accountCity.text.toString()) {
                    city = accountCity.text.toString()
                    mUserInfoDBEntry.city = city
                }

                if (mUserInfoDBEntry.postCode != accountPostCode.text.toString()) {
                    postCode = accountPostCode.text.toString()
                    mUserInfoDBEntry.postCode = postCode
                }

                if (mUserInfoDBEntry.email != accountEmail.text.toString()) {
                    email = accountEmail.text.toString()
                    mUserInfoDBEntry.email = email
                }

                val dBUpdateCallback = object : TaskCompletionManager {
                    override fun onSuccess() {
                        Toast.makeText(context, "Data saved", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure() {
                        TODO("Not yet implemented")
                    }
                }
                mUserInfoDBEntry.updateDBFields(dBUpdateCallback)

                // Go back to the Profile menu
                mNavigatorManager.navigator().back()
            }

            accountBack.setOnClickListener {
                // Go back to the Profile menu
                mNavigatorManager.navigator().back()
            }

            accountLogOut.setOnClickListener{
                AppUser.getInstance().logOut()

                // Delete the app current user
                val pref: SharedPreferences = (mActivity!! as Context).getSharedPreferences(
                        getString(R.string.lib_name), Context.MODE_PRIVATE)
                pref.edit().putString(getString(R.string.app_uid), "").commit()

                onLogout()
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser) {
            Log.d("EBT", "Account page becomes visible")

            mUserInfoDBEntry.key = AppUser.getInstance().id
            mUserInfoDBEntry.readDBFields(object : TaskCompletionManager {
                override fun onSuccess() {
                    mUserFirstName.value = mUserInfoDBEntry.firstName
                    mUserLastName.value = mUserInfoDBEntry.lastName
                    mUserAddress.value = mUserInfoDBEntry.address
                    mUserCity.value = mUserInfoDBEntry.city
                    mUserPostcode.value = mUserInfoDBEntry.postCode
                    mUserEmail.value = mUserInfoDBEntry.email
                    mUserScore.value = mUserInfoDBEntry.getScore()
                }

                override fun onFailure() {}
            })
        } else {
            Log.d("EBT", "Account page becomes hidden")
        }
    }

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