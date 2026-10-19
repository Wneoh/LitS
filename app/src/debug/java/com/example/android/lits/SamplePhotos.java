package com.example.android.lits;

import android.content.Context;
import java.util.ArrayList;

/** Free Pexels photos bundled only with debug previews. See PHOTO_CREDITS.md. */
final class SamplePhotos {
    static ArrayList<String> forPost(Context context, int post) {
        int[][] albums = {
            {R.drawable.sample_346529, R.drawable.sample_1525066},
            {R.drawable.sample_6942518, R.drawable.sample_11012098, R.drawable.sample_17666419},
            {R.drawable.sample_8022599, R.drawable.sample_17666419, R.drawable.sample_1525066, R.drawable.sample_346529}
        };
        ArrayList<String> result = new ArrayList<>();
        for (int resource : albums[post % albums.length])
            result.add("android.resource://" + context.getPackageName() + "/" + resource);
        return result;
    }
}
