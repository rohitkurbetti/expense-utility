package com.example.expenseutility;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Log;
import android.view.SurfaceView;

import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectDetector {

    private final Interpreter tflite;
    private final Context context;
    private List<String> labels;

    public ObjectDetector(Context context) {
        this.context = context;
        this.labels = loadLabels("labelmap.txt");
        try {
            tflite = new Interpreter(loadModelFile("detect.tflite"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> loadLabels(String filename) {
        List<String> labels = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(filename)));
            String line;
            while ((line = reader.readLine()) != null) {
                labels.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return labels;
    }

    public void analyze(ImageProxy imageProxy, SurfaceView overlay) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            Bitmap bitmap = toBitmap(mediaImage);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
            ByteBuffer input = convertBitmapToByteBuffer(resized);

            float[][][] outputLocations = new float[1][10][4];
            float[][] outputClasses = new float[1][10];
            float[][] outputScores = new float[1][10];
            float[] numDetections = new float[1];

            Map<Integer, Object> outputMap = new HashMap<>();
            outputMap.put(0, outputLocations);
            outputMap.put(1, outputClasses);
            outputMap.put(2, outputScores);
            outputMap.put(3, numDetections);

            tflite.runForMultipleInputsOutputs(new Object[]{input}, outputMap);
            drawResults(outputLocations[0], outputClasses[0], outputScores[0], overlay);
        }
        imageProxy.close();
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1 * 300 * 300 * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[300 * 300];
        bitmap.getPixels(intValues, 0, 300, 0, 0, 300, 300);
        for (int pixel : intValues) {
            byte r = (byte) ((pixel >> 16) & 0xFF);
            byte g = (byte) ((pixel >> 8) & 0xFF);
            byte b = (byte) (pixel & 0xFF);
            byteBuffer.put(r);
            byteBuffer.put(g);
            byteBuffer.put(b);
        }
        return byteBuffer;
    }

    private MappedByteBuffer loadModelFile(String model) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(model);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    private Bitmap toBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, android.graphics.ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, out);
        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private void drawResults(float[][] boxes, float[] classes, float[] scores, SurfaceView overlay) {
        Canvas canvas = overlay.getHolder().lockCanvas();
        if (canvas == null) return;

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        Paint boxPaint = new Paint();
        boxPaint.setColor(Color.RED);
        boxPaint.setStrokeWidth(4);
        boxPaint.setStyle(Paint.Style.STROKE);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);

        int canvasWidth = overlay.getWidth();
        int canvasHeight = overlay.getHeight();

        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > 0.3) {
                float top = Math.max(0, boxes[i][0]) * canvasHeight;
                float left = Math.max(0, boxes[i][1]) * canvasWidth;
                float bottom = Math.min(1, boxes[i][2]) * canvasHeight;
                float right = Math.min(1, boxes[i][3]) * canvasWidth;

                // Clamp coordinates to screen
                top = Math.max(0, Math.min(top, canvasHeight));
                left = Math.max(0, Math.min(left, canvasWidth));
                bottom = Math.max(0, Math.min(bottom, canvasHeight));
                right = Math.max(0, Math.min(right, canvasWidth));

                canvas.drawRect(left, top, right, bottom, boxPaint);
//                canvas.drawText("Obj " + ((int) classes[i]) + ": " + (int) (scores[i] * 100) + "%", left+30, top + 30, textPaint);

                int labelIndex = (int) classes[i];
                String labelName = labelIndex < labels.size() ? labels.get(labelIndex) : "Unknown";
                canvas.drawText(labelName + ": " + (int)(scores[i] * 100) + "%", left+30, top +30, textPaint);

            }
        }

        overlay.getHolder().unlockCanvasAndPost(canvas);
    }


}
