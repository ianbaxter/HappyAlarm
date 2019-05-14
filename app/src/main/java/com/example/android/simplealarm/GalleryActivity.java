package com.example.android.simplealarm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.simplealarm.adapters.EmptyRecyclerView;
import com.example.android.simplealarm.adapters.GalleryAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = GalleryActivity.class.getSimpleName();

    private static final int DETAIL_PHOTO_REQUEST = 1;
    private static final String CURRENT_POSITION_KEY = "current_position";

    GalleryAdapter galleryAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        TextView emptyView = findViewById(R.id.tv_empty_view_gallery);
        EmptyRecyclerView recyclerView = findViewById(R.id.recycler_view_gallery);
        setTitle("Gallery");

        File dir = this.getFilesDir();
        File[] files = dir.listFiles();
        List<File> filesList = Arrays.asList(files);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        galleryAdapter = new GalleryAdapter(this, filesList);
        recyclerView.setAdapter(galleryAdapter);
        recyclerView.setEmptyView(emptyView);
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_gallery, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_slideshow:
                faceSlideshow();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == DETAIL_PHOTO_REQUEST && resultCode == RESULT_OK) {
            int deletedPosition;
            if (data != null && data.hasExtra(CURRENT_POSITION_KEY)) {
                deletedPosition = data.getIntExtra(CURRENT_POSITION_KEY, 0);
                galleryAdapter.notifyItemRemoved(deletedPosition);
            }
        }
    }

    public void faceSlideshow() {
        Toast.makeText(this, R.string.fab_slideshow_clicked, Toast.LENGTH_SHORT).show();

        // Load all images, detect and crop all faces
        // and create/show video showing each cropped face for ~1 second
        /*FileInputStream fileInputStream = null;
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
                                    Rect bounds = face.getBoundingBox();

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
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e);
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e);
            }
        }*/
    }
}
