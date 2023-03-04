//
//  ActivityWithAsyncTask.java
//
//  Created by Mathieu Delehaye on 4/03/2023.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
//
//  Copyright © 2023 Mathieu Delehaye. All rights reserved.
//
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//  FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

package com.beautyorder.androidclient;

public interface ActivityWithAsyncTask {

    // Callback function to check if the condition, depending on the environment, is fulfilled before 
    // running some actions.
    boolean environmentCondition();

    // Callback function to check if the condition, depending on the time, is fulfilled before
    // running some actions.
    boolean timeCondition(long cumulatedTimeInSec);

    // Callback function to run actions depending on the environment.
    void runEnvironmentDependentActions();

    // Callback function to run actions depending on the time.
    void runTimesDependentActions();
}
