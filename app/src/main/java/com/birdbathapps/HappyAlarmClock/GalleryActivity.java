package com.birdbathapps.HappyAlarmClock;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.birdbathapps.HappyAlarmClock.adapters.EmptyRecyclerView;
import com.birdbathapps.HappyAlarmClock.adapters.GalleryAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import timber.log.Timber;

public class GalleryActivity extends AppCompatActivity {

    private static final int DETAIL_PHOTO_REQUEST = 1;
    private static final int OPEN_CAMERA_REQUEST = 2;
    private static final String CURRENT_POSITION_KEY = "current_position";

    private ArrayList<File> fileArrayList;
    private GalleryAdapter galleryAdapter;
    private EmptyRecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        setTitle(R.string.title_gallery_activity);
        TextView emptyView = findViewById(R.id.tv_empty_view_gallery);
        recyclerView = findViewById(R.id.recycler_view_gallery);
        FloatingActionButton cameraFab = findViewById(R.id.fab_take_photo);

        createListOfFiles();
        createView(emptyView, recyclerView, cameraFab);
    }

    private void createListOfFiles() {
        File dir = this.getFilesDir();
        File[] files;
        try {
            files = dir.listFiles();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            } else {
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            }
            fileArrayList = new ArrayList<>(Arrays.asList(files));
        } catch (Exception e) {
            Timber.e(e,"Exception");
        }
    }

    private void createView(TextView emptyView, EmptyRecyclerView recyclerView, FloatingActionButton fab) {
        RecyclerView.LayoutManager layoutManager;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager = new GridLayoutManager(this, 2);
        } else {
            layoutManager = new GridLayoutManager(this, 3);
        }
        recyclerView.setLayoutManager(layoutManager);
        galleryAdapter = new GalleryAdapter(this, fileArrayList);
        recyclerView.setAdapter(galleryAdapter);
        recyclerView.setEmptyView(emptyView);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0 && !fab.isShown()) {
                    fab.show();
                } else if (dy > 0 && fab.isShown()) {
                    fab.hide();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DETAIL_PHOTO_REQUEST && resultCode == RESULT_OK ) {
            if (data != null && data.hasExtra(CURRENT_POSITION_KEY)) {
                int deletedPosition = data.getIntExtra(CURRENT_POSITION_KEY, 0);
                fileArrayList.remove(deletedPosition);
                galleryAdapter.notifyItemRemoved(deletedPosition);
            }
        }

        if (requestCode == OPEN_CAMERA_REQUEST && resultCode == RESULT_OK) {
            File dir = this.getFilesDir();
            File[] files;
            try {
                files = dir.listFiles();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                } else {
                    Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                }
                ArrayList<File> tempFileArrayList = new ArrayList<>(Arrays.asList(files));
                File newPhotoFile = tempFileArrayList.get(0);
                fileArrayList.add(0, newPhotoFile);
                galleryAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                Timber.e(e,"Exception");
            }
        }
    }

    public void startCameraActivity(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, OPEN_CAMERA_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_graph_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_graph:
                if (fileArrayList.size() > 1) {
                    Intent intent = new Intent(this, GraphActivity.class);
                    startActivity(intent);
                } else {
                    Snackbar.make(recyclerView, R.string.snackbar_graph_data_error_text, Snackbar.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
