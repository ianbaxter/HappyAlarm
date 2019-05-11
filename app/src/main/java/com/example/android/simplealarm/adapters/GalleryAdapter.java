package com.example.android.simplealarm.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.simplealarm.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private GalleryItemClickListener galleryItemClickListener;
    private Context mContext;

    private static final String TAG = GalleryAdapter.class.getSimpleName();

    public GalleryAdapter(Context context) {
        mContext = context;
    }

    public interface GalleryItemClickListener {
        void onItemClick(String fileName);
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public ImageView galleryImageView;

        public GalleryViewHolder(View galleryView) {
            super(galleryView);

            galleryImageView = galleryView.findViewById(R.id.image_view_gallery_item);
            galleryImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.i(TAG, "Clicked position: " + position);

            FileInputStream fileInputStream = null;
            try {
                String[] fileNames = mContext.fileList();
                String fileName = fileNames[position];
                fileInputStream = mContext.openFileInput(fileName);
                File file = new File(mContext.fileList()[position]);
                file.delete();
//                Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
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

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);

        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        File dir = mContext.getFilesDir();
        File[] files = dir.listFiles();
        Picasso.get().load(files[position]).into(holder.galleryImageView);
    }

    @Override
    public int getItemCount() {
        String[] fileNames = mContext.fileList();
        return fileNames.length;
    }
}
