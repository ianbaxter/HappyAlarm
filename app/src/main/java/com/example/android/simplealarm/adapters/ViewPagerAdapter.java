package com.example.android.simplealarm.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.android.simplealarm.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {

    private Context context;
    private List<File> filesList;

    public ViewPagerAdapter(Context context, List<File> filesList) {
        this.context = context;
        this.filesList = filesList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Picasso.get()
                    .load(filesList.get(position))
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.outline_image_24)
                    .into(imageView);
        } else {
            Picasso.get()
                    .load(filesList.get(position))
                    .placeholder(R.drawable.outline_image_24)
                    .into(imageView);
        }
        container.addView(imageView);

        return imageView;
    }

    @Override
    public int getCount() {
        return filesList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
