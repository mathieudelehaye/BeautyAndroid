package com.beautyorder.androidclient.controller.main.qrcode;

public interface QRCodeFoundListener {
    void onQRCodeFound(String qrCode);
    void qrCodeNotFound();
}