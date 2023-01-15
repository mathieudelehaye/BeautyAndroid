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

package com.beautyorder.androidclient.controller.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.databinding.FragmentCameraBinding;
import com.beautyorder.androidclient.model.UserInfoEntry;
import com.beautyorder.androidclient.qrcode.QRCodeFoundListener;
import com.beautyorder.androidclient.qrcode.QRCodeImageAnalyzer;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

public class FragmentCamera extends Fragment {
    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private FragmentCameraBinding binding;
    private Context mCtx;
    private PreviewView mPreviewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private String mQRCode;
    private SharedPreferences mSharedPref;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        mQRCode = "";
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCtx = view.getContext();

        mPreviewView = view.findViewById(R.id.activity_main_previewView);

        cameraProviderFuture = ProcessCameraProvider.getInstance(mCtx);

        // Get the app preferences
        mSharedPref = mCtx.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText((Activity) mCtx, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
            ArrayList<String> permissionsToRequest = new ArrayList<>();
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

        Preview preview = new Preview.Builder()
            .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build();

        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());

        ImageAnalysis imageAnalysis =
            new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(mCtx), new QRCodeImageAnalyzer(
            new QRCodeFoundListener() {
                @Override
                public void onQRCodeFound(String _qrCode) {
                    processQRCode(_qrCode);
                }

                @Override
                public void qrCodeNotFound() {
                    Log.d("BeautyAndroid", "QR Code Not Found");
                }
        }));

        // avoid having too many use cases, when switching back to the camera screen
        cameraProvider.unbindAll();

        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }

    public void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(mCtx, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(mCtx));
    }

    private void processQRCode(String _qrCode) {

        if (_qrCode.equals(mQRCode)) {
            return;
        }

        Log.d("BeautyAndroid", "New QR Code Found: " + _qrCode);

        mQRCode = _qrCode;

        // Check if no QR code scanning has already been sent for today. If no, add the scanning event
        // to the queue, so it is sent later.
        HashSet<String> scoreQueue = (HashSet<String>) mSharedPref.getStringSet("set",
            new HashSet<String>());

        HashSet<String> updatedQueue = (HashSet<String>)scoreQueue.clone();

        String timeStamp = UserInfoEntry.scoreTimeFormat.format(new java.util.Date());

        if (!updatedQueue.contains(timeStamp)) {
            updatedQueue.add(timeStamp);

            // Write back the queue to the app preferences
            mSharedPref.edit().putStringSet(getString(R.string.scores_to_send), updatedQueue).commit();

            Log.i("BeautyAndroid", "Scanning event added to the queue for the timestamp: "
                + timeStamp);

            Toast toast = Toast.makeText(mCtx, "Thanks for recycling!", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
    }
}