package com.example.android.simplealarm;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.android.simplealarm.adapters.ViewPagerAdapter;

import java.io.File;

public class GalleryDetailActivity extends AppCompatActivity {

    private static final String TAG = GalleryDetailActivity.class.getSimpleName();
    private static final String GALLERY_POSITION_KEY = "photo_position";
    private static final String CURRENT_POSITION_KEY = "current_position";

    private int mCurrentPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_detail);
        setTitle(null);

        File dir = this.getFilesDir();
        File[] files = dir.listFiles();
        mCurrentPosition = getPositionFromParentIntent();

        ViewPager viewPager = findViewById(R.id.view_pager_gallery_detail);
        viewPager.setOffscreenPageLimit(4);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, files);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(mCurrentPosition);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_gallery_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_delete:
                showDeleteDialog(this);
                break;
            case R.id.action_share:
                sharePhoto();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Delete photo")
                .setMessage("Are you sure you wish to delete this photo?")
                .setPositiveButton(R.string.dialog_confirm_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePhoto();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel_delete, null)
                .show();
    }

    public void deletePhoto() {
        File file = getFileFromPosition(mCurrentPosition);
        boolean fileDeleted = file.delete();
        if (fileDeleted) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(CURRENT_POSITION_KEY, mCurrentPosition);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            Log.e(TAG, "Error deleting file");
        }
    }

    public void sharePhoto() {
        File file = getFileFromPosition(mCurrentPosition);
        Uri imageUri = FileProvider.getUriForFile(this,
                this.getApplicationContext().getPackageName() + ".provider",
                file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share using"));
    }

    private int getPositionFromParentIntent() {
        int position = 0;
        if (getIntent() != null && getIntent().hasExtra(GALLERY_POSITION_KEY)) {
            Intent intent = getIntent();
            position = intent.getIntExtra(GALLERY_POSITION_KEY, 0);
        }
        return position;
    }

    private File getFileFromPosition(int position) {
        File dir = this.getFilesDir();
        File[] files = dir.listFiles();
        return files[position];
    }
}