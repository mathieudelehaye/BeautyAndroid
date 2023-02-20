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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class FragmentCamera extends Fragment {
    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private FragmentCameraBinding mBinding;
    private Context mCtx;
    private PreviewView mPreviewView;
    private ListenableFuture<ProcessCameraProvider> mCameraProviderFuture;
    private SharedPreferences mSharedPref;
    private Date mLastPhotoTaking;
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
        super.onViewCreated(view, savedInstanceState);

        mCtx = view.getContext();

        mPreviewView = view.findViewById(R.id.preview_camera);

        mCameraProviderFuture = ProcessCameraProvider.getInstance(mCtx);

        mSharedPref = mCtx.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        mLastPhotoTaking = Helpers.parseTime(UserInfoDBEntry.scoreTimeFormat,
            mSharedPref.getString("photo_taking_date", "1970.01.01"));

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
        if (ContextCompat.checkSelfPermission(mCtx, Manifest.permission.CAMERA)
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

        mBinding.takePhotoCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // If a picture has already been taken the same day, do nothing
                final var currentDate = new java.util.Date();
                if (mLastPhotoTaking.compareTo(currentDate) >= 0) {
                    return;
                }

                Log.i("BeautyAndroid", "Capturing an image with the camera");
                Toast.makeText(mCtx, "Capturing image", Toast.LENGTH_SHORT).show();

                String fileName = AppUser.getInstance().getId() + "-"
                    + (new SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss").format(new java.util.Date()));

                String appFolderPath = "/storage/emulated/0/Android/data/com.beautyorder.androidclient/files";
                var file = new File(appFolderPath, fileName);

                ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(file).build();

                imageCapture.takePicture(outputFileOptions,
                    Executors.newSingleThreadExecutor(),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                            Log.d("BeautyAndroid", "Image saved to file: " + file);
                            Log.v("BeautyAndroid", "Image saved to file at timestamp: "
                                + Helpers.getTimestamp());

                            var path = file.getPath();

                            // TODO: store the File object reference in a queue for sending
                        }
                        @Override
                        public void onError(ImageCaptureException error) {
                            Log.e("BeautyAndroid", "Error while saving the image: " + error.toString());
                        }
                    }
                );
            }
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
                var dialogFragment = new FragmentHelpDialog("Take photos of your empty beauty products on "
                    + "the counter to get EBpoints!");
                dialogFragment.show(getChildFragmentManager(), "Camera help dialog");
            }
        }
    }
}