package com.example.expenseutility;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.example.expenseutility.databinding.ActivityCaptureBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaptureActivity extends AppCompatActivity {


    private ActivityCaptureBinding binding;
    private File tempPhotoFile;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_capture);

        binding = ActivityCaptureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Request camera permissions
//        if (allPermissionsGranted()) {
            startCamera();
//        } else {
//            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
//        }

        binding.captureButton.setOnClickListener(v -> takePhoto());

        cameraExecutor = Executors.newSingleThreadExecutor();


    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (allPermissionsGranted()) {
//                startCamera();
//            } else {
//                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        }
//    }


//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        cameraExecutor.shutdown();
//    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            return;
        }

        tempPhotoFile = new File(getCacheDir(), System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(tempPhotoFile).build();
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // Show the image preview dialog when the image is saved
                        if(tempPhotoFile.exists()) {
                            showImagePreviewDialog(tempPhotoFile);
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(getApplicationContext(), "Photo capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void showImagePreviewDialog(File tempPhotoFile) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.camera_preview);
        dialog.setCancelable(false);
        // Get the ImageView from the dialog and set the photo
        ImageView previewImageView = dialog.findViewById(R.id.previewImageView);
        Bitmap bitmap = BitmapFactory.decodeFile(tempPhotoFile.getAbsolutePath());
        previewImageView.setImageBitmap(bitmap);

        // Set up buttons for "Recapture" and "Select"
        Button recaptureButton = dialog.findViewById(R.id.recaptureButton);
        Button selectButton = dialog.findViewById(R.id.selectButton);

        recaptureButton.setOnClickListener(v -> {
            // If recapture is clicked, delete the temp file and dismiss dialog
            if (tempPhotoFile.exists()) {
                tempPhotoFile.delete();
            }
            dialog.dismiss();
//            takePhoto(); // Trigger photo capture again
        });

        selectButton.setOnClickListener(v -> {
            // Confirm photo selection and save the file
            String savedFilePath = savePhotoToStorage(tempPhotoFile); // Modify this method to return the path
            // Return the filename to the previous activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("photoFileName", savedFilePath); // Return saved file path
            setResult(RESULT_OK, resultIntent); // Set the result as OK with the file name

            // Close the dialog and finish the activity
            dialog.dismiss();
            finish(); // End this activity and return to the previous one
        });

        dialog.show();
    }

    private String savePhotoToStorage(File tempPhotoFile) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File parentFolderPath = new File(downloadsDir.getAbsolutePath()+File.separator+"Expense Captures");
        if(!parentFolderPath.exists()) {
            parentFolderPath.mkdirs();
        }
        File photoFile = new File(parentFolderPath.getAbsolutePath(), "IMG_"+new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(new Date().getTime()) + ".jpg");


        try {
            // Copy the temporary file to the permanent location
            copyFile(tempPhotoFile, photoFile);
            if(tempPhotoFile.exists()){
                tempPhotoFile.delete();
            }
            // Show a toast with the file path
            Toast.makeText(CaptureActivity.this, "Photo saved: " + photoFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            return photoFile.getAbsolutePath(); // Return the file path
        } catch (IOException e) {
            Toast.makeText(CaptureActivity.this, "Failed to save photo", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return null;
    }

    private void copyFile(File src, File dst) throws IOException {
        try (FileOutputStream out = new FileOutputStream(dst)) {
            Bitmap bitmap = BitmapFactory.decodeFile(src.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
    }

}