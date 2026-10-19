package com.example.android.lits;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public final class PostPhotoEditor {
    public static final int MAX_PHOTOS = 6;
    private final AppCompatActivity activity;
    private final ArrayList<Uri> uris = new ArrayList<>();
    private int selectedIndex;
    public PostPhotoEditor(AppCompatActivity activity, Bundle state) {
        this.activity = activity;
        ActivityResultLauncher<String[]> picker = activity.registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(), selected -> {
                    for (Uri uri : selected) {
                        if (uris.contains(uri)) continue;
                        if (uris.size() >= MAX_PHOTOS) { Toast.makeText(activity, "Up to 6 photos per entry", Toast.LENGTH_SHORT).show(); break; }
                        try { activity.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
                        catch (SecurityException ignored) { }
                        uris.add(uri);
                    }
                    render();
                });
        activity.findViewById(R.id.add_photo).setOnClickListener(v -> picker.launch(new String[]{"image/*"}));
        activity.findViewById(R.id.remove_photo).setOnClickListener(v -> {
            if (!uris.isEmpty()) uris.remove(selectedIndex);
            selectedIndex = Math.max(0, Math.min(selectedIndex, uris.size() - 1));
            render();
        });
        if (state != null) {
            ArrayList<String> saved = state.getStringArrayList("postPhotos");
            if (saved != null) for (String uri : saved) uris.add(Uri.parse(uri));
            else if (state.getString("postPhoto") != null) uris.add(Uri.parse(state.getString("postPhoto")));
        }
        render();
    }
    public List<Uri> getUris() { return new ArrayList<>(uris); }
    public void save(Bundle state) {
        ArrayList<String> saved = new ArrayList<>();
        for (Uri uri : uris) saved.add(uri.toString());
        state.putStringArrayList("postPhotos", saved);
    }
    private void render() {
        ImageView image = activity.findViewById(R.id.editor_photo);
        image.setTag(uris.isEmpty() ? null : uris.get(selectedIndex));
        activity.findViewById(R.id.photo_preview_group).setVisibility(uris.isEmpty() ? View.GONE : View.VISIBLE);
        Picasso.with(activity).cancelRequest(image);
        image.setImageDrawable(null);
        if (!uris.isEmpty()) Picasso.with(activity).load(uris.get(selectedIndex)).resize(1200, 900).centerInside().into(image);
        ((TextView) activity.findViewById(R.id.remove_photo)).setText("Remove photo " + (selectedIndex + 1));
        LinearLayout thumbnails = activity.findViewById(R.id.photo_thumbnails);
        thumbnails.removeAllViews();
        for (int i = 0; i < uris.size(); i++) {
            final int index = i;
            ImageView thumb = new ImageView(activity);
            int size = Math.round(56 * activity.getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMarginEnd(12);
            thumbnails.addView(thumb, params);
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.setAlpha(i == selectedIndex ? 1f : .55f);
            thumb.setContentDescription("Select photo " + (i + 1));
            thumb.setOnClickListener(v -> { selectedIndex = index; render(); });
            Picasso.with(activity).load(uris.get(i)).resize(160, 160).centerCrop().into(thumb);
        }
        ((TextView) activity.findViewById(R.id.add_photo)).setText(uris.isEmpty() ? "＋ Photos" : "＋ Photos (" + uris.size() + "/6)");
        EditText text = activity.findViewById(R.id.para);
        activity.findViewById(R.id.post_para).setEnabled(!uris.isEmpty() || !text.getText().toString().trim().isEmpty());
    }
}
