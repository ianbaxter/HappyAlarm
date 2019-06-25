package com.example.android.simplealarm;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.Nullable;

import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;
import com.example.android.simplealarm.utilities.NotificationUtils;
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
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    private static String TAG = CameraActivity.class.getSimpleName();
    private static final String ALARM_DISMISS_KEY = "dismiss_alarm";
    private static final String RECEIVER_INTENT_FILTER = "com.alarm.AUTO_SILENT";

    private CameraView cameraView;
    private TextView clockText;
    private SwipeButton snoozeSwipeButton;
    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        intent = getIntent();

        setupViews(intent);
        showOnLockScreen();
        setupCameraView(intent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(RECEIVER_INTENT_FILTER);
        registerReceiver(autoSilentReceiver, filter);
    }

    private BroadcastReceiver autoSilentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finishAndRemoveTask();
        }
    };

    private void setupViews(Intent intent) {
        clockText = findViewById(R.id.text_clock_smile);
        snoozeSwipeButton = findViewById(R.id.swipe_btn_alarm_snooze);
        snoozeSwipeButton.setOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(boolean active) {
                snoozeFromCamera();
            }
        });
        if (intent != null && intent.hasExtra(ALARM_DISMISS_KEY)) {
            snoozeSwipeButton.setVisibility(View.VISIBLE);
            clockText.setVisibility(View.VISIBLE);
        } else {
            snoozeSwipeButton.setVisibility(View.GONE);
            clockText.setVisibility(View.GONE);
        }
    }

    private void showOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        } else {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    private void setupCameraView(Intent intent) {
        cameraView = findViewById(R.id.view_camera);
        cameraView.setLifecycleOwner(this);
        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                savePhoto(result);
            }
        });
        cameraView.addFrameProcessor(new FrameProcessor() {
            @Override
            public void process(@NonNull Frame frame) {
                byte[] data = frame.getData();
                int rotation = frame.getRotation() / 90;
                Size size = frame.getSize();

                if (size != null) {
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
                                                return;
                                            }
                                            for (FirebaseVisionFace face : faces) {
                                                if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                    float smileProb = face.getSmilingProbability();

                                                    if (intent.getExtras() != null && intent.hasExtra(ALARM_DISMISS_KEY)) {
                                                        if (smileProb > 0.85 && AlarmReceiver.mediaPlayer != null) {
                                                            AlarmReceiver.stopAlarm(getApplicationContext());
                                                            clockText.setVisibility(View.GONE);
                                                            snoozeSwipeButton.setVisibility(View.GONE);
                                                            cameraView.takePicture();
                                                        }
                                                    } else {
                                                        if (smileProb > 0.85) {
                                                            clockText.setVisibility(View.GONE);
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
            }
        });
    }

    private void savePhoto(@NonNull PictureResult result) {
        File photoFile = createImageFile();
        result.toFile(photoFile, new FileCallback() {
            @Override
            public void onFileReady(@Nullable File file) {
                setResult(RESULT_OK, null);
                if (intent != null && intent.hasExtra(ALARM_DISMISS_KEY)) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }
            }
        });
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        return new File(this.getFilesDir(), imageFileName + ".jpg");
    }

    public void snoozeFromCamera() {
        int alarmEntryId = intent.getIntExtra(ALARM_DISMISS_KEY, 0);
        NotificationUtils.snoozeAlarm(this, alarmEntryId);
        finishAndRemoveTask();
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
        unregisterReceiver(autoSilentReceiver);
    }

    @Override
    public void onBackPressed() {
        if (intent != null && intent.hasExtra(ALARM_DISMISS_KEY)) {
            // Do nothing
        } else {
            super.onBackPressed();
        }
    }
}
