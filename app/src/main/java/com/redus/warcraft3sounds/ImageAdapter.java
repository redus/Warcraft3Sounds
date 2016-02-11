package com.redus.warcraft3sounds;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Adapter for MainActivity GridView.
 * Return Bitmap of each unit for getItem.
 */
public class ImageAdapter extends BaseAdapter {

    private UnitDatabase unitDatabase;
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
        unitDatabase = UnitDatabase.getInstance();
    }

    public int getCount() {
        return unitDatabase.getCount();
    }

    public Bitmap getItem(int position) {
        return unitDatabase.getUnitImage(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        Bitmap bitmap = getItem(position);
        imageView.setImageBitmap(bitmap);
        return imageView;
    }

}
