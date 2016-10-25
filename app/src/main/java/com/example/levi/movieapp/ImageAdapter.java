package com.example.levi.movieapp;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

// a subclass of baseadapter that uses an arraylist to contain image urls
public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mImgUrls.size();
    }

    public String getItem(int position) {
        return mImgUrls.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public void clear(){
        mImgUrls = new ArrayList<String>();
    }

    public void add(String thumbId){
        mImgUrls.add(thumbId);
        notifyDataSetChanged();
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(201, 294));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
            imageView.setBackground(mContext.getDrawable(R.drawable.border_image));
        } else {
            imageView = (ImageView) convertView;
        }

        // use picasso to load image into view
        Picasso
                .with(mContext)
                .load(mImgUrls.get(position))
                .resize(185, 278)
                .into(imageView);

        return imageView;
    }

    // references to our images
    private ArrayList<String> mImgUrls = new ArrayList<String>();
}