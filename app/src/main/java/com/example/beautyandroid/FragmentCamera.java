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

package com.beautyorder.androidclient;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import com.beautyorder.androidclient.databinding.FragmentCameraBinding;
import com.example.beautyandroid.qrcode.QRCodeFoundListener;
import com.example.beautyandroid.qrcode.QRCodeImageAnalyzer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FragmentCamera extends Fragment {
    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private FragmentCameraBinding binding;
    private Context mCtx;
    private Activity mActivity;
    private PreviewView mPreviewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private String mQRCode;
    private FirebaseFirestore mDatabase;

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
        mActivity = (Activity) mCtx;

        mPreviewView = view.findViewById(R.id.activity_main_previewView);

        Log.d("BeautyAndroid", "mdl onViewCreated 1");
        cameraProviderFuture = ProcessCameraProvider.getInstance(mCtx);
        requestCamera();

        // Get the DB
        mDatabase = FirebaseFirestore.getInstance();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("BeautyAndroid", "mdl FragmentCamera::onRequestPermissionsResult entered");

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
    public void onPause() {
        super.onPause();
    }

    private void requestCamera() {
        Log.d("BeautyAndroid", "mdl requestCamera entered");

        if (ContextCompat.checkSelfPermission(mCtx, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            Log.d("BeautyAndroid", "mdl requestCamera: camera permission not granted");

            // Permission is not granted
            ArrayList<String> permissionsToRequest = new ArrayList<>();
            permissionsToRequest.add(Manifest.permission.CAMERA);

            requestPermissions(
                permissionsToRequest.toArray(new String[0]),
                PERMISSION_REQUEST_CAMERA);
        } else {
            Log.d("BeautyAndroid", "mdl requestCamera: camera permission granted");
            startCamera();
        }
    }

    void updateScore(String documentId, Integer newValue) {

        // Get a new write batch
        WriteBatch batch = mDatabase.batch();

        DocumentReference ref = mDatabase.collection("userInfos").document(documentId);
        batch.update(ref, "score", newValue);

        // Commit the batch
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });

    }

    void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Log.d("BeautyAndroid", "mdl bindCameraPreview entered");

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

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(mCtx), new QRCodeImageAnalyzer(new QRCodeFoundListener() {
            @Override
            public void onQRCodeFound(String _qrCode) {

                Log.d("BeautyAndroid", "QR Code Found: " + _qrCode);

                if (_qrCode.equals(mQRCode)) {
                    Log.w("BeautyAndroid", "QR Code already scanned");
                    return;
                }

                mQRCode = _qrCode;

                // Update user score
                // TODO: create asynchronous process if no network is when while scanning the QR code
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser user = auth.getCurrentUser();
                FirebaseFirestore database = FirebaseFirestore.getInstance();

                Log.d("BeautyAndroid", "onQRCodeFound: user.getUid() = " + user.getUid().toString());

                database.collection("userInfos")
                    .whereEqualTo("__name__", "mathieu.delehaye@gmail.com")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            // Display score
                            Integer userScore = 0;

                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("BeautyAndroid", document.getId() + " => " + document.getData());

                                    userScore = Integer.parseInt(document.getData().get("score").toString());
                                    Log.d("BeautyAndroid", "onQRCodeFound: userScore = " + String.valueOf(userScore));

                                    userScore += 1;
                                    updateScore(document.getId(), userScore);

                                    Toast toast = Toast.makeText(mCtx, "Thanks for recycling!", Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                    toast.show();

                                    // Update the score in the map view
                                    TextView scoreTextArea = (TextView) mActivity.findViewById(R.id.textArea_score);
                                    scoreTextArea.setText(String.valueOf(userScore) + " pts");

                                    Log.d("BeautyAndroid", "Score updated");

                                    break;
                                }
                            } else {
                                Log.d("BeautyAndroid", "Error getting documents: ", task.getException());
                            }
                        }
                    });
            }

            @Override
            public void qrCodeNotFound() {
                Log.d("BeautyAndroid", "QR Code Not Found");
            }
        }));

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }

    public void startCamera() {
        Log.d("BeautyAndroid", "mdl startCamera entered");

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(mCtx, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(mCtx));
    }
}