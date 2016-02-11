package com.redus.warcraft3sounds;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import java.io.File;

/**
 * Created by redus on 2015-11-20.
 */
public class Sound {

    public Sound(Context c, int source) {
        this.mContext = c;
        this.source = source;
    }

    @Override
    public String toString() {
        Uri path = Uri.parse("android.resource://" + mContext.getPackageName() + File.separator + source);
        mediaMetadataRetriever.setDataSource(mContext, path);
        return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
    }

    public int getSource() {
        return source;
    }

    private static MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    private int source;
    private Context mContext;
}
