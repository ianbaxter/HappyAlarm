package com.example.android.simplealarm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Frame;
import com.otaliastudios.cameraview.FrameProcessor;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.Size;

import java.io.File;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private static String TAG = CameraActivity.class.getSimpleName();
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    CameraView cameraView;
    TextView smileText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        smileText = findViewById(R.id.text_view_smile);
        smileText.setVisibility(View.VISIBLE);

        cameraView = findViewById(R.id.view_camera);
        cameraView.setLifecycleOwner(this);

        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                checkPermissionsAndSavePhoto(result);
            }
        });

        cameraView.addFrameProcessor(new FrameProcessor() {
            @Override
            public void process(@NonNull Frame frame) {
                byte[] data = frame.getData();
                int rotation = frame.getRotation() / 90;
                Size size = frame.getSize();

                // Process
                FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                        .setWidth(size.getWidth())
                        .setHeight(size.getHeight())
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                        .setRotation(rotation)
                        .build();

                FirebaseVisionImage image = FirebaseVisionImage.fromByteArray(data, metadata);

                FirebaseVisionFaceDetectorOptions realTimeOpts =
                        new FirebaseVisionFaceDetectorOptions.Builder()
                                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                                .build();

                FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                        .getVisionFaceDetector(realTimeOpts);


                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        if (faces.size() == 0) {
                                            return;
                                        }

                                        for (FirebaseVisionFace face : faces) {

                                            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                float smileProb = face.getSmilingProbability();
                                                Log.i(TAG, "Smiling prob: " + smileProb);

                                                if (smileProb > 0.9 && AlarmReceiver.mediaPlayer != null) {
                                                    AlarmReceiver.stopAlarm();
                                                    smileText.setVisibility(View.GONE);
                                                    cameraView.takePicture();
                                                    finish();
                                                }
                                            }
                                        }
                                    }
                                });
            }
        });
    }

    private void checkPermissionsAndSavePhoto(@NonNull PictureResult result) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            savePhoto(result);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    savePhoto();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void savePhoto(@NonNull PictureResult result) {
        File photoFile = null;

//        result.toFile(photoFile, );
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }
}
