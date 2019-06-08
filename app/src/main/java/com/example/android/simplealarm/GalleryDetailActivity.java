package com.example.android.simplealarm;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.android.simplealarm.adapters.ViewPagerAdapter;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GalleryDetailActivity extends AppCompatActivity {

    private static final String TAG = GalleryDetailActivity.class.getSimpleName();

    private static final String GALLERY_POSITION_KEY = "photo_position";
    private static final String CURRENT_POSITION_KEY = "current_position";

    private List<File> fileList;
    private int currentPosition;
    private ActionBar actionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_detail);
        actionBar = getSupportActionBar();
        ViewPager viewPager = findViewById(R.id.view_pager_gallery_detail);

        createListOfFiles();
        createView(viewPager);
    }

    private void setTitle(ActionBar actionBar, int position) {
        String photoFileName = fileList.get(position).getName();
        String[] parts = photoFileName.split("_");
        String[] dateParts = parts[1].split("");
        String[] timeParts = parts[2].split("");
        String title = dateParts[7] + dateParts[8] + "/" + dateParts[5] + dateParts[6] + "/"
                + dateParts[1] + dateParts[2] + dateParts[3] + dateParts[4] + "   "
                + timeParts[1] + timeParts[2] + ":" + timeParts[3] + timeParts[4];

        actionBar.setTitle(title);
    }

    private void createListOfFiles() {
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
                        return Long.compare(f2.lastModified(), f1.lastModified());
                    } });
            }
            fileList = Arrays.asList(files);
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e);
        }
    }

    private void createView(ViewPager viewPager) {
        currentPosition = getPositionFromParentIntent();
        setTitle(actionBar, currentPosition);
        viewPager.setOffscreenPageLimit(3);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, fileList);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(currentPosition);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                setTitle(actionBar, currentPosition);
            }
        });
    }

    private int getPositionFromParentIntent() {
        int position = 0;
        if (getIntent() != null && getIntent().hasExtra(GALLERY_POSITION_KEY)) {
            Intent intent = getIntent();
            position = intent.getIntExtra(GALLERY_POSITION_KEY, 0);
        }
        return position;
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
                .setTitle(R.string.alert_dialog_title_delete_photo)
                .setMessage(R.string.alert_dialog_message_delete_photo)
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
        File file = fileList.get(currentPosition);
        boolean fileDeleted = file.delete();
        if (fileDeleted) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(CURRENT_POSITION_KEY, currentPosition);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            Log.e(TAG, "Error deleting file");
        }
    }

    public void sharePhoto() {
        File file = fileList.get(currentPosition);
        Uri imageUri = FileProvider.getUriForFile(this,
                this.getApplicationContext().getPackageName() + ".provider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_photo_title)));
    }
}
