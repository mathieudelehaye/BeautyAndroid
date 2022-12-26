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
//  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//  FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

package com.example.beautyandroid;

import android.util.Log;
import com.example.beautyandroid.model.UserInfoEntry;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserInfoEntryUnitTest {
    private Map<String, Object> mUserInfoMap = new HashMap<>();

    String unitTestUser = "unit-test-user" ;

    public UserInfoEntryUnitTest() {
        mUserInfoMap.put("first_name", "Mathieu");
        mUserInfoMap.put("last_name", "Delehaye");
        mUserInfoMap.put("address", "15, Granville street");
        mUserInfoMap.put("city", "Glasgow");
        mUserInfoMap.put("post_code", "G3 7EE");
        mUserInfoMap.put("score", 10);
        mUserInfoMap.put("score_time", "2022.12.20");
    }

    @Test
    public void updateScore() {

        // TODO: add a mock class to replace the Firebase DB
        UserInfoEntry entry = new UserInfoEntry(null, unitTestUser, mUserInfoMap);

        {
            final int entryScore = entry.getScore();
            assertEquals(10, entryScore);
        }

        {
            entry.setScore(15);
            final int entryScore = entry.getScore();
            assertEquals(15, entryScore);
        }
    }

    @Test
    public void updateScoreTime() {

        // TODO: add a mock class to replace the Firebase DB
        UserInfoEntry entry = new UserInfoEntry(null, unitTestUser, mUserInfoMap);

        {
            final Date entryScoreTime = entry.getScoreTime();
            assertEquals(true, entryScoreTime.equals(UserInfoEntry.parseScoreTime("2022.12.20")));
        }

        {
            entry.setScoreTime("2022.12.22");
            final Date entryScoreTime = entry.getScoreTime();
            assertEquals(true, entryScoreTime.equals(UserInfoEntry.parseScoreTime("2022.12.22")));
        }
    }
}
