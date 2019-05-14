package com.example.android.simplealarm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.FileCallback;
import com.otaliastudios.cameraview.Frame;
import com.otaliastudios.cameraview.FrameProcessor;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.Size;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private static String TAG = CameraActivity.class.getSimpleName();
    private static final String ALARM_DISMISS_KEY = "dismiss_alarm";

    private CameraView cameraView;
    private TextView smileText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        smileText = findViewById(R.id.text_view_smile);
        smileText.setVisibility(View.VISIBLE);

        final Intent intent = getIntent();

        cameraView = findViewById(R.id.view_camera);
        cameraView.setLifecycleOwner(this);

        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                savePhoto(result);
                finish();
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
                                .setMinFaceSize(0.15f)
                                .build();

                FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                        .getVisionFaceDetector(realTimeOpts);

                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        if (faces.size() == 0) {
//                                            cameraView.takePicture(); // For emulator testing only
                                            return;
                                        }

                                        for (FirebaseVisionFace face : faces) {

                                            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                float smileProb = face.getSmilingProbability();
                                                Log.i(TAG, "Smiling probability: " + smileProb);

                                                if (intent.getExtras() != null && intent.hasExtra(ALARM_DISMISS_KEY)) {
                                                    if (smileProb > 0.9 && AlarmReceiver.mediaPlayer != null) {
                                                        AlarmReceiver.stopAlarm();
                                                        smileText.setVisibility(View.GONE);
                                                        cameraView.takePicture();
                                                    }
                                                } else {
                                                    if (smileProb > 0.9) {
                                                        smileText.setVisibility(View.GONE);
                                                        cameraView.takePicture();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Exception" + e);
                    }
                });
            }
        });
    }

    private void savePhoto(@NonNull PictureResult result) {
        File photoFile = createImageFile();

        Log.i(TAG, "Saving photo to internal storage");
        result.toFile(photoFile, new FileCallback() {
            @Override
            public void onFileReady(@Nullable File file) {

            }
        });
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        return new File(this.getFilesDir(), imageFileName + ".jpg");
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
