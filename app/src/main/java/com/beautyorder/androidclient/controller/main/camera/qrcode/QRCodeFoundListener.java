package com.beautyorder.androidclient.controller.main.camera.qrcode;

public interface QRCodeFoundListener {
    void onQRCodeFound(String qrCode);
    void qrCodeNotFound();
}