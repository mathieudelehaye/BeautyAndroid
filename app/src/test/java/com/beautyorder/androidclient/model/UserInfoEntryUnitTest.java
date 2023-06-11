//
//  UserInfoEntryUnitTest.java
//
//  Created by Mathieu Delehaye on 26/12/2022.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
//
//  Copyright © 2022 Mathieu Delehaye. All rights reserved.
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

package com.beautyorder.androidclient.model;

import com.android.java.androidjavatools.Helpers;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserInfoEntryUnitTest {
    String unitTestUser = "unit-test-user" ;

    public UserInfoEntryUnitTest() {
    }

    @Test
    public void createUserInfo() {

        Map<String, String> mUserInfoMap = new HashMap<>();

        mUserInfoMap.put("first_name", "Mathieu");
        mUserInfoMap.put("last_name", "Delehaye");
        mUserInfoMap.put("address", "15, Granville street");
        mUserInfoMap.put("city", "Glasgow");
        mUserInfoMap.put("post_code", "G3 7EE");
        mUserInfoMap.put("email", "mathieu.delehaye@gmail.com");

        mUserInfoMap.put("score", "10");
        mUserInfoMap.put("score_time", "2022.12.20");

        mUserInfoMap.put("ordered_sample_key", "");

        mUserInfoMap.put("device_id", "5d944db5c143e59b");

        var database = (new FirebaseFirestoreMockManager()).getDatabase();
        var entry = new EBUserInfoDBEntry(database, unitTestUser, mUserInfoMap);

        entry.createAllDBFields();
    }

    @Test
    public void updateScore() {

        var database = (new FirebaseFirestoreMockManager()).getDatabase();
        var entry = new EBUserInfoDBEntry(database, unitTestUser);

        {
            final int entryScore = entry.getScore();
            assertEquals(0, entryScore);
        }

        {
            entry.setScore(15);
            final int entryScore = entry.getScore();
            assertEquals(15, entryScore);
            entry.updateDBFields();
        }
    }

    @Test
    public void updateScoreTime() {

        var database = (new FirebaseFirestoreMockManager()).getDatabase();
        var entry = new EBUserInfoDBEntry(database, unitTestUser);

        {
            final Date entryScoreTime = entry.getScoreTime();
            assertEquals(true, entryScoreTime.equals(Helpers.parseTime(EBUserInfoDBEntry.scoreTimeFormat,
                "1970.01.01")));
        }

        {
            entry.setScoreTime("2022.12.22");
            final Date entryScoreTime = entry.getScoreTime();
            assertEquals(true, entryScoreTime.equals(Helpers.parseTime(EBUserInfoDBEntry.scoreTimeFormat,
                "2022.12.22")));
            entry.updateDBFields();
        }
    }
}
