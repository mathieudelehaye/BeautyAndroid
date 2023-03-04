//  FragmentCamera.java
//
//  Created by Mathieu Delehaye on 17/12/2022.
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

package com.beautyorder.androidclient.controller.main.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.beautyorder.androidclient.Helpers;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.controller.main.CollectionPagerAdapter;
import com.beautyorder.androidclient.controller.main.MainActivity;
import com.beautyorder.androidclient.controller.main.dialog.FragmentHelpDialog;
import com.beautyorder.androidclient.databinding.FragmentCameraBinding;
import com.beautyorder.androidclient.model.AppUser;
import com.beautyorder.androidclient.model.UserInfoDBEntry;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class FragmentCamera extends Fragment {
    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private FragmentCameraBinding mBinding;
    private Context mCtx;
    private PreviewView mPreviewView;
    private ListenableFuture<ProcessCameraProvider> mCameraProviderFuture;
    private SharedPreferences mSharedPref;
    private boolean mIsViewVisible = false;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        mBinding = FragmentCameraBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.v("BeautyAndroid", "Camera view created at timestamp: "
            + Helpers.getTimestamp());

        super.onViewCreated(view, savedInstanceState);

        mCtx = view.getContext();

        mPreviewView = view.findViewById(R.id.preview_camera);

        mCameraProviderFuture = ProcessCameraProvider.getInstance(mCtx);

        mSharedPref = mCtx.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        showHelp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(mCtx, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            mIsViewVisible = true;

            Log.d("BeautyAndroid", "Camera view becomes visible");

            CollectionPagerAdapter.setPage(1);

            requestCamera();

            var activity = (MainActivity)getActivity();
            if ((activity) != null) {
                activity.enableTabSwiping();
            }

            showHelp();
        } else {
            mIsViewVisible = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void requestCamera() {
        if (mCtx != null && ContextCompat.checkSelfPermission(mCtx, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            var permissionsToRequest = new ArrayList<String>();
            permissionsToRequest.add(Manifest.permission.CAMERA);

            requestPermissions(
                permissionsToRequest.toArray(new String[0]),
                PERMISSION_REQUEST_CAMERA);
        } else {
            startCamera();
        }
    }

    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {

        mPreviewView.setPreferredImplementationMode(PreviewView.ImplementationMode.SURFACE_VIEW);

        var preview = new Preview.Builder()
            .build();

        var cameraSelector = new CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build();

        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());

        var imageCapture =
            new ImageCapture.Builder()
                .setTargetRotation(mPreviewView.getDisplay().getRotation())
                .setTargetResolution(new Size(480, 640))
                .build();

        mBinding.takePhotoCamera.setOnClickListener(view -> {

            // If a picture has already been taken the same day, do nothing
            final var currentDate = new java.util.Date();
            final var lastPhotoDate = Helpers.parseTime(UserInfoDBEntry.scoreTimeFormat,
                mSharedPref.getString(getString(R.string.photo_date), "1970.01.01"));

            if (Helpers.compareYearDays(lastPhotoDate, currentDate) >= 0) {
                Log.d("BeautyAndroid", "A photo has already been taken today");

                var dialogFragment = new FragmentHelpDialog("You can only send one photo a day");
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
                                var dialogFragment = new FragmentHelpDialog(
                                "The photo has been correctly taken and will be sent for verification.\n\nYou "
                                    + "will receive a notification, within 24 hours, when your points have been "
                                    + "added.");
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

        // avoid having too many use cases, when switching back to the camera screen
        cameraProvider.unbindAll();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    public void startCamera() {
        mCameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = mCameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(mCtx, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(mCtx));
    }

    private void showHelp() {

        if (mIsViewVisible && mSharedPref != null) {
            if (!Boolean.parseBoolean(mSharedPref.getString("cam_help_displayed", "false"))) {
                mSharedPref.edit().putString("cam_help_displayed", "true").commit();
                var dialogFragment = new FragmentHelpDialog("Take a photo of your beauty containers on "
                    + "the drop-off location counter and receive EBpoints!");
                dialogFragment.show(getChildFragmentManager(), "Camera help dialog");
            }
        }
    }
}