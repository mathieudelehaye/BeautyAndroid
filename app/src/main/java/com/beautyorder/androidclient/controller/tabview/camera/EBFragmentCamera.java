//
//  EBFragmentCamera.java
//
//  Created by Mathieu Delehaye on 20/04/2023.
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

package com.beautyorder.androidclient.controller.tabview.camera;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import com.android.java.androidjavatools.Helpers;
import com.android.java.androidjavatools.controller.tabview.camera.FragmentCamera;
import com.android.java.androidjavatools.model.AppUser;
import com.beautyorder.androidclient.controller.tabview.CollectionPagerAdapter;
import com.beautyorder.androidclient.controller.tabview.TabViewActivity;
import com.beautyorder.androidclient.controller.tabview.dialog.FragmentHelpDialog;
import com.beautyorder.androidclient.model.UserInfoDBEntry;
import com.beautyorder.androidclient.R;
import java.io.File;
import java.util.HashSet;
import java.util.concurrent.Executors;

public class EBFragmentCamera extends FragmentCamera {
    private SharedPreferences mSharedPref;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSharedPref = mContext.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        showHelp();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            CollectionPagerAdapter.setPage(1);

            var activity = (TabViewActivity)getActivity();
            if ((activity) != null) {
                activity.toggleTabSwiping(true);
            }

            showHelp();
        }
    }

    @Override
    protected void defineCameraButtonBehaviour(ImageCapture imageCapture) {

        mBinding.takePhotoCamera.setOnClickListener(view -> {

            // If a picture has already been taken the same day, do nothing
            final var currentDate = new java.util.Date();
            final var lastPhotoDate = Helpers.parseTime(UserInfoDBEntry.scoreTimeFormat,
                mSharedPref.getString(getString(R.string.photo_date), "1970.01.01"));

            if (Helpers.compareYearDays(lastPhotoDate, currentDate) >= 0) {
                Log.d("BeautyAndroid", "A photo has already been taken today");

                var dialogFragment = new FragmentHelpDialog(getString(R.string.one_photo_by_day));
                dialogFragment.show(getChildFragmentManager(), "Camera no more photo dialog");

                return;
            }

            Log.i("BeautyAndroid", "Capturing a photo with the camera");

            String photoDate = UserInfoDBEntry.scoreTimeFormat.format(currentDate);
            String fileName = AppUser.getInstance().getId() + "-" + photoDate;

            String appFolderPath = "/storage/emulated/0/Android/data/com.beautyorder.androidclient/files";
            var file = new File(appFolderPath, fileName);

            ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(file).build();

            imageCapture.takePicture(outputFileOptions,
                Executors.newSingleThreadExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        Log.d("BeautyAndroid", "Photo saved to file: " + file);
                        Log.v("BeautyAndroid", "Photo saved at timestamp: "
                                + Helpers.getTimestamp());

                        mSharedPref.edit().putString(getString(R.string.photo_date), photoDate).commit();

                        String filePath = file.getPath();

                        // Store the local file path in a queue to send the file to the DB
                        var photoQueue = (HashSet<String>) (mSharedPref.getStringSet(
                                getString(R.string.photos_to_send), new HashSet<>()));

                        if (!photoQueue.contains(filePath)) {
                            photoQueue.add(filePath);

                            // Write back the queue to the app preferences
                            mSharedPref.edit().putStringSet(getString(R.string.photos_to_send), photoQueue)
                                .commit();

                            Log.i("BeautyAndroid", "Photo added to the queue for sending");

                            final Activity activity = getActivity();
                            activity.runOnUiThread(() -> {
                                var dialogFragment = new FragmentHelpDialog(getString(R.string.photo_correctly_sent));
                                dialogFragment.show(getChildFragmentManager(), "Camera photo taken dialog");
                            });
                        }
                    }
                    @Override
                    public void onError(ImageCaptureException error) {
                        Log.e("BeautyAndroid", "Error while saving the image: " + error.toString());
                    }
                }
            );
        });
    }

    private void showHelp() {
        if (mIsViewVisible && mSharedPref != null) {
            if (!Boolean.parseBoolean(mSharedPref.getString("cam_help_displayed", "false"))) {
                mSharedPref.edit().putString("cam_help_displayed", "true").commit();
                var dialogFragment = new FragmentHelpDialog(getString(R.string.camera_help));
                dialogFragment.show(getChildFragmentManager(), "Camera help dialog");
            }
        }
    }
}
