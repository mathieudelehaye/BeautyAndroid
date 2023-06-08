//
//  OnboardActivity.java
//
//  Created by Mathieu Delehaye on 14/01/2023.
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

package com.beautyorder.androidclient.controller.onboard;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.beautyorder.androidclient.R;

public class OnboardActivity extends Activity implements GestureDetector.OnGestureListener {

    private FragmentOnboard mFragment;
    private GestureDetector mGestureDetector;
    final private int mSwipeThreshold = 100;
    final private int mSwipeVelocityThreshold = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding);

        var manager = getFragmentManager();
        mFragment = (FragmentOnboard) manager
            .findFragmentById(R.id.main_onboarding_fragment);

        mGestureDetector = new GestureDetector(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        Log.v("EBT", "Tap gesture on the onboarding screen");
        if (mFragment.isLastPageReached()) {
            mFragment.onFinishFragment();
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        return;
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        try {
            final float diffY = motionEvent.getY() - motionEvent1.getY();
            final float diffX = motionEvent.getX() - motionEvent1.getX();

            if (Math.abs(diffX) > Math.abs(diffY)) {


                if (Math.abs(diffX) > mSwipeThreshold && Math.abs(v) > mSwipeVelocityThreshold) {
                    if (diffX > 0) {
                        Log.v("EBT", "Left to Right swipe gesture on the onboarding screen");
                        mFragment.moveToNextPage();
                    }
                    else {
                        Log.v("EBT", "Right to Left swipe gesture on the onboarding screen");
                        mFragment.moveToPreviousPage();
                    }
                }
            }
        }
        catch (Exception e) {
            Log.e("EBT", "An error occurred during the swipe gesture");
            return false;
        }
        return true;
    }
}
