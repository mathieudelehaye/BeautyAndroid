//
//  EBUserInfoDBEntry.java
//
//  Created by Mathieu Delehaye on 27/05/2023.
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

import com.android.java.androidjavatools.Helpers
import com.android.java.androidjavatools.model.TaskCompletionManager
import com.android.java.androidjavatools.model.UserInfoDBEntry
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EBUserInfoDBEntry : UserInfoDBEntry {
    companion object {
        @JvmField
        var scoreTimeFormat = SimpleDateFormat("yyyy.MM.dd")
    }
    private var mScoreTime: Date? = null

    constructor(database : FirebaseFirestore, key : String, data : Map<String, String>) :
        super(database, key, data) {

        val dataItem = mData[0] // only 1 document for the user
        dataItem["score"] = ""
        dataItem["score_time"] = "1970.01.01"
        mScoreTime = Helpers.parseTime(scoreTimeFormat, data["score_time"])
    }

    constructor(database : FirebaseFirestore, key : String) :
        super(database, key) {

        mScoreTime = Helpers.parseTime(scoreTimeFormat, "1970.01.01")
    }

    override fun initializeDataChange() {
        super.initializeDataChange()
        val dataChangeItem = mDataChanged[0] // only 1 document for the user
        dataChangeItem["score"] = false
        dataChangeItem["score_time"] = false
    }

    fun getScore(): Int {
        val score = mData[0]["score"]
        return if (score !== "") score!!.toInt() else 0
    }

    fun setScore(value: Int) {
        mData[0]["score"] = value.toString()
        mDataChanged[0]["score"] = true
    }

    fun getScoreTime(): Date? {
        return Helpers.parseTime(scoreTimeFormat, mData[0]["score_time"])
    }

    fun setScoreTime(value: String?) {
        mScoreTime = Helpers.parseTime(scoreTimeFormat, value)
        mData[0]["score_time"] = value
        mDataChanged[0]["score_time"] = true
    }

    fun readScoreDBFields(vararg cbManager: TaskCompletionManager?): Boolean {
        val fields = arrayOf("score", "score_time")
        return readDBFieldsForCurrentKey(fields, *cbManager)
    }
}