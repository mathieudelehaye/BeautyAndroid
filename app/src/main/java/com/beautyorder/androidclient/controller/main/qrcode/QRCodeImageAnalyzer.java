package com.beautyorder.androidclient.controller.main.qrcode;

import android.annotation.SuppressLint;
import android.graphics.*;
import android.media.Image;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.demo.BitmapUtils;

import java.util.List;


public class QRCodeImageAnalyzer implements ImageAnalysis.Analyzer {
    private QRCodeFoundListener listener;

    private Boolean imageSaved = false;

    public QRCodeImageAnalyzer(QRCodeFoundListener listener) {
        this.listener = listener;
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {

        @SuppressLint("UnsafeOptInUsageError") Image mediaImage = image.getImage();
        if (mediaImage != null) {

            // Uncomment to read a QR code from a png image
            /*String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Pictures/Screenshots";
            File dir = new File(file_path);
            File file = new File(dir, "camera_input.png");
            Bitmap bpm = BitmapFactory.decodeFile(file.getPath());*/

            @SuppressLint("UnsafeOptInUsageError") Bitmap bpm = BitmapUtils.getBitmap(image);

            InputImage img = InputImage.fromBitmap(bpm, 0);

            BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_AZTEC)
                    .build();

            BarcodeScanner scanner = BarcodeScanning.getClient(options);

            Task<List<Barcode>> result = scanner.process(img)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        for (Barcode barcode: barcodes) {
                            Rect bounds = barcode.getBoundingBox();
                            Point[] corners = barcode.getCornerPoints();

                            String rawValue = barcode.getRawValue();

                            Log.v("BeautyAndroid", "Bar code raw value: " + rawValue);

                            int valueType = barcode.getValueType();
                            // See API reference for complete list of supported types
                            switch (valueType) {
                                case Barcode.TYPE_URL:
                                    String title = barcode.getUrl().getTitle();
                                    String url = barcode.getUrl().getUrl();
                                    Log.v("BeautyAndroid", "Bar code url: " + url);
                                    listener.onQRCodeFound(url);
                                    return;
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("BeautyAndroid", "Image processing failed");
                        listener.qrCodeNotFound();
                    }
                });
        }

        image.close();
    }
}