package com.example.android.simplealarm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.simplealarm.adapters.EmptyRecyclerView;
import com.example.android.simplealarm.adapters.GalleryAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = GalleryActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        TextView emptyView = findViewById(R.id.tv_empty_view_gallery);
        EmptyRecyclerView recyclerView = findViewById(R.id.recycler_view_gallery);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        GalleryAdapter galleryAdapter = new GalleryAdapter(this);
        recyclerView.setAdapter(galleryAdapter);
        recyclerView.setEmptyView(emptyView);
        recyclerView.setHasFixedSize(true);
    }

    public void faceSlideshow(View view) {
        Toast.makeText(this, R.string.fab_slideshow_clicked, Toast.LENGTH_SHORT).show();

        // Load all images, detect and crop all faces
        // and create/show video showing each cropped face for ~1 second
        FileInputStream fileInputStream = null;
        try {
            String[] fileNames = this.fileList();
            int numberOfPhotos = fileNames.length;
            for (int i = 0; i < numberOfPhotos - 1; i++) {
                String fileName = fileNames[i];
                fileInputStream = this.openFileInput(fileName);
                Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);

                // ML Kit
                FirebaseVisionFaceDetectorOptions options =
                        new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setMinFaceSize(0.15f)
                        .build();

                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

                FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                        .getVisionFaceDetector(options);

                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionFace> faces) {
                                for (FirebaseVisionFace face : faces) {

                                }
                            }
                        });
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e);
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e);
            }
        }
    }
}
