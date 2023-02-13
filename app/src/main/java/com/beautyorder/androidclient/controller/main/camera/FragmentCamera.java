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
import androidx.lifecycle.LifecycleOwner;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.controller.main.CollectionPagerAdapter;
import com.beautyorder.androidclient.databinding.FragmentCameraBinding;
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
    private String mQRCode;
    private SharedPreferences mSharedPref;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        mBinding = FragmentCameraBinding.inflate(inflater, container, false);
        mQRCode = "";
        return mBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCtx = view.getContext();

        mPreviewView = view.findViewById(R.id.preview_camera);

        mCameraProviderFuture = ProcessCameraProvider.getInstance(mCtx);

        mSharedPref = mCtx.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);
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
            Log.d("BeautyAndroid", "Camera view becomes visible");

            CollectionPagerAdapter.setAppPage(1);

            requestCamera();
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
                .build();

        mBinding.takePhotoCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("BeautyAndroid", "Capturing an image with the camera");

                String appFolderPath = "/storage/emulated/0/Android/data/com.beautyorder.androidclient/files";
                var filePath = new File(appFolderPath, (new SimpleDateFormat("yyyy.MM.dd"))
                    .format(new Date())+ ".jpg");
                Log.v("BeautyAndroid", "mdl file = "+ filePath);

                ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(filePath).build();

                imageCapture.takePicture(outputFileOptions,
                    Executors.newSingleThreadExecutor(),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                            Log.v("BeautyAndroid", "Image saved to file");
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

        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageCapture, preview);
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
}