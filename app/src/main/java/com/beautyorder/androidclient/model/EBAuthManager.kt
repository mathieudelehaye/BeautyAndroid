//
//  EBAuthManager.kt
//
//  Created by Mathieu Delehaye on 5/06/2023.
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

package com.beautyorder.androidclient.model

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.os.SystemClock
import android.util.Log
import com.android.java.androidjavatools.Helpers
import com.android.java.androidjavatools.model.AuthManager
import com.android.java.androidjavatools.model.AppUser
import com.android.java.androidjavatools.model.TaskCompletionManager
import com.beautyorder.androidclient.R
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class EBAuthManager(activity : Activity) : AuthManager(activity) {

    override fun onSignup(credentials: MutableMap<String, String>?) {
        credentials?.put("score", "0")
        credentials?.put("score_time", EBUserInfoDBEntry.scoreTimeFormat.format(
            Helpers.getDayBeforeDate(Helpers.getDayBeforeDate(Date()))))

        mActivity.getSharedPreferences(
            mActivity.getString(R.string.app_name), MODE_PRIVATE).getString(
            mActivity.getString(com.android.java.androidjavatools.R.string.device_id), "")?.let {
                credentials?.put("device_id", it) }

        val userInfo = EBUserInfoDBEntry(mDatabase, credentials?.get("email")!!, credentials)
        userInfo.createAllDBFields()

        SystemClock.sleep(1000)

        // Navigate to the next dialog to show
        mNavigator.showFragment("login")
    }

    override fun onAnonymousUserCreation(userId : String, creationDate : Date,
        cbManager: TaskCompletionManager?) {

        // Add userInfos table entry to the database for the anonymous user
        val userInfo = EBUserInfoDBEntry(mDatabase, userId)
        userInfo.setScoreTime(EBUserInfoDBEntry.scoreTimeFormat.format(
            Helpers.getDayBeforeDate(creationDate)))
        userInfo.deviceId = mSharedPref.getString(mActivity.getString(
            com.android.java.androidjavatools.R.string.device_id), "")

        userInfo.createAllDBFields(object : TaskCompletionManager {
            override fun onSuccess() {
                val uidText: String = userId
                Log.d("EBT", "The user automatic identifier was created: "
                    + uidText)
                setAnonymousUidToPreferences(uidText)
                startAppWithUser(uidText, AppUser.AuthenticationType.NOT_REGISTERED)
            }

            override fun onFailure() {
                cbManager!!.onFailure()
            }
        })
    }

    override fun onSignin(userId : String) {
        ScoreTransferer(FirebaseFirestore.getInstance(),
            anonymousUidFromPreferences,
            userId, mActivity)
            .run()
    }
}
