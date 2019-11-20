package com.birdbathapps.HappyAlarmClock.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.birdbathapps.HappyAlarmClock.GalleryDetailActivity;
import com.birdbathapps.HappyAlarmClock.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private static final int DETAIL_PHOTO_REQUEST = 1;
    private final String GALLERY_POSITION_KEY = "photo_position";

    private Context context;
    private ArrayList<File> filesList;

    public GalleryAdapter(Context context, ArrayList<File> files) {
        this.context = context;
        filesList = files;
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

            Intent intent = new Intent(context, GalleryDetailActivity.class);
            intent.putExtra(GALLERY_POSITION_KEY, position);
            ((Activity) context).startActivityForResult(intent, DETAIL_PHOTO_REQUEST);
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
                .load(filesList.get(position))
                .centerCrop()
                .fit()
                .placeholder(R.drawable.outline_image_24)
                .into(holder.galleryImageView);
    }

    @Override
    public int getItemCount() {
        if (filesList == null) {
            return 0;
        }
        return filesList.size();
    }
}
