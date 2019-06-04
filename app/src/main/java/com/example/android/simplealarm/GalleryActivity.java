package com.example.android.simplealarm;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.simplealarm.adapters.EmptyRecyclerView;
import com.example.android.simplealarm.adapters.GalleryAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = GalleryActivity.class.getSimpleName();

    private static final int DETAIL_PHOTO_REQUEST = 1;
    private static final String CURRENT_POSITION_KEY = "current_position";

    private ArrayList<File> filesList;
    private GalleryAdapter galleryAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        TextView emptyView = findViewById(R.id.tv_empty_view_gallery);
        EmptyRecyclerView recyclerView = findViewById(R.id.recycler_view_gallery);
        setTitle(R.string.title_gallery_activity);

        File dir = this.getFilesDir();
        File[] files;
        try {
            files = dir.listFiles();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            } else {
                Arrays.sort(files, new Comparator<File>(){
                    public int compare(File f1, File f2)
                    {
                        return Long.compare(f1.lastModified(), f2.lastModified());
                    } });
            }
            filesList = new ArrayList<>(Arrays.asList(files));
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e);
        }

        RecyclerView.LayoutManager layoutManager;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager = new GridLayoutManager(this, 2);
        } else {
            layoutManager = new GridLayoutManager(this, 3);
        }
        recyclerView.setLayoutManager(layoutManager);
        galleryAdapter = new GalleryAdapter(this, filesList);
        recyclerView.setAdapter(galleryAdapter);
        recyclerView.setEmptyView(emptyView);
        recyclerView.setHasFixedSize(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DETAIL_PHOTO_REQUEST && resultCode == RESULT_OK) {
            int deletedPosition;
            if (data != null && data.hasExtra(CURRENT_POSITION_KEY)) {
                deletedPosition = data.getIntExtra(CURRENT_POSITION_KEY, 0);
                filesList.remove(deletedPosition);
                galleryAdapter.notifyItemRemoved(deletedPosition);
            }
        }
    }
}
