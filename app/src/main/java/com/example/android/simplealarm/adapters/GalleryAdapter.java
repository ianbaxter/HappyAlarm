package com.example.android.simplealarm.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.simplealarm.GalleryDetailActivity;
import com.example.android.simplealarm.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private Context mContext;
    private static List<File> mFiles;

    private static final String GALLERY_POSITION_KEY = "photo_position";
    private static final int DETAIL_PHOTO_REQUEST = 1;

    private static final String TAG = GalleryAdapter.class.getSimpleName();

    public GalleryAdapter(Context context, List<File> files) {
        mContext = context;
        mFiles = files;
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private ImageView galleryImageView;

        private GalleryViewHolder(View galleryView) {
            super(galleryView);

            galleryImageView = galleryView.findViewById(R.id.image_view_gallery_item);
            galleryImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.i(TAG, "Clicked position: " + position);

            Intent intent = new Intent(mContext, GalleryDetailActivity.class);
            intent.putExtra(GALLERY_POSITION_KEY, position);
            ((Activity) mContext).startActivityForResult(intent, DETAIL_PHOTO_REQUEST);
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
        Picasso.get()
                .load(mFiles.get(position))
                .placeholder(R.drawable.outline_image_24)
                .into(holder.galleryImageView);
    }

    @Override
    public int getItemCount() {
        if (mFiles == null) {
            return 0;
        }
        return mFiles.size();
    }
}
