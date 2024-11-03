package com.example.expenseutility.utility;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.EncodeHintType;

import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    public Bitmap generateQRCode(String userDetails) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            // Define the size of the QR code
            int width = 500;
            int height = 500;

            // Setting the Error Correction Level to high
            Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            // Encode the user details into the QR code
            BitMatrix bitMatrix = qrCodeWriter.encode(userDetails, BarcodeFormat.QR_CODE, width, height, hintMap);

            // Create a Bitmap to represent the QR code
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            // Generate the QR code bitmap
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}

