package com.example.android.lits;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import com.google.android.material.navigation.NavigationView;

/** Debug builds only. No authentication, database reads, or writes. */
public class DesignPreviewActivity extends BaseActivity {
    private PostPhotoEditor photoEditor;
    private static final java.util.Map<String, java.util.List<Comment>> conversations = new java.util.HashMap<>();
    private CommentsUi commentsUi;
    private void bindComments(String key) {
        java.util.List<Comment> conversation = conversations.get(key);
        if (conversation == null) {
            conversation = new java.util.ArrayList<>();
            conversation.add(new Comment("sample-one", "Oliver James", "This feels like a moment worth keeping.", System.currentTimeMillis() - 720000));
            conversation.add(new Comment("sample-two", "Aisha Rahman", "Love the way you've captured this. Thanks for sharing!", System.currentTimeMillis() - 240000));
            conversations.put(key, conversation);
        }
        final java.util.List<Comment> messages = conversation;
        commentsUi = new CommentsUi(this, text -> {
            messages.add(new Comment("preview-user", "You · Preview", text, System.currentTimeMillis()));
            commentsUi.sent(); commentsUi.render(messages);
        });
        commentsUi.render(messages);
    }
    private final java.util.List<java.util.ArrayList<String>> samplePhotos = new java.util.ArrayList<>();
    private static final java.util.Map<String, java.util.Map<String, String>> previewReactions = new java.util.HashMap<>();

    private void bindReactions(View root, String entry) {
        java.util.Map<String, String> reactions = previewReactions.get(entry);
        if (reactions == null) {
            reactions = new java.util.HashMap<>();
            reactions.put("sample-1", "love");
            reactions.put("sample-2", "love");
            reactions.put("sample-3", "wow");
            previewReactions.put(entry, reactions);
        }
        final java.util.Map<String, String> values = reactions;
        ReactionBar.bind(root.findViewById(R.id.post_reactions), values, "preview-user", key -> {
            String next = PostReactions.toggle(values.get("preview-user"), key);
            if (next == null) values.remove("preview-user"); else values.put("preview-user", next);
            bindReactions(root, entry);
        });
    }
    private final String[][] entries = {
        {"Maya Chen", "Sample entry · Reflection", "Some days, the smallest things stay with us the longest. A kind word. A familiar song. The light through a window."},
        {"Oliver James", "Sample entry · Short prose", "I took the long way home today. Somewhere between the quiet streets and the evening sky, I found a thought worth keeping."},
        {"Aisha Rahman", "Sample entry · Poetry", "Leave a little room\nfor the unexpected.\nEven an unfinished page\nis a beginning."}
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if ("reader".equals(getIntent().getStringExtra("screen"))) {
            setContentView(R.layout.activity_click_post);
            ((TextView) findViewById(R.id.click_post)).setText(
                    "Sample entry\n\n" + getIntent().getStringExtra("entry"));
            findViewById(R.id.read_back).setOnClickListener(view -> finish());
            ((TextView) findViewById(R.id.reader_author)).setText(getIntent().getStringExtra("author") == null ? "Sample author" : getIntent().getStringExtra("author"));
            ((TextView) findViewById(R.id.reader_date)).setText("Sample entry · Just now");
            String author = ((TextView) findViewById(R.id.reader_author)).getText().toString();
            ((TextView) findViewById(R.id.reader_initial)).setText(author.isEmpty() ? "?" : author.substring(0, 1));
            java.util.ArrayList<String> photos = getIntent().getStringArrayListExtra("imageUrls");
            if (photos == null) {
                photos = new java.util.ArrayList<>();
                if (getIntent().getStringExtra("imageUrl") != null) photos.add(getIntent().getStringExtra("imageUrl"));
            }
            PostPhotos.bindDetails(findViewById(android.R.id.content), photos);
            bindReactions(findViewById(android.R.id.content), getIntent().getStringExtra("entry"));
            bindComments(getIntent().getStringExtra("entry"));
            return;
        }
        if ("editor".equals(getIntent().getStringExtra("screen"))) {
            setContentView(R.layout.activity_para);
            Toolbar toolbar = findViewById(R.id.update_para_toolbar);
            toolbar.setTitle("New entry");
            toolbar.setSubtitle("Preview only");
            toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
            toolbar.setNavigationContentDescription("Back to feed");
            toolbar.setNavigationOnClickListener(v -> finish());
            EditorUi.bind(this);
            photoEditor = new PostPhotoEditor(this, state);
            findViewById(R.id.post_para).setOnClickListener(v ->
                Toast.makeText(this, "Preview only — your words haven’t been published.", Toast.LENGTH_LONG).show());
            return;
        }
        setContentView(R.layout.activity_main);
        for (int i = 0; i < entries.length; i++) samplePhotos.add(SamplePhotos.forPost(this, i));
        Toolbar toolbar = findViewById(R.id.main_page_toolbar);
        toolbar.setTitle("LitS.");
        toolbar.setSubtitle("Sample feed");
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationContentDescription("Open navigation");
        DrawerLayout drawer = findViewById(R.id.drawable_layout);
        toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
        NavigationView navigation = findViewById(R.id.navigation_view);
        View header = navigation.inflateHeaderView(R.layout.navigation_header);
        ((TextView) header.findViewById(R.id.nav_user_full_name)).setText("Design preview");
        navigation.setNavigationItemSelectedListener(item -> {
            drawer.closeDrawers();
            if (item.getItemId() == R.id.nav_post) openEditor();
            if (item.getItemId() == R.id.nav_theme) startActivity(new Intent(this, ThemeActivity.class));
            if (item.getItemId() == R.id.nav_logout) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            return true;
        });
        findViewById(R.id.feed_status).setVisibility(View.GONE);
        findViewById(R.id.add_new_para).setOnClickListener(v -> openEditor());
        RecyclerView list = findViewById(R.id.all_users_post_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new RecyclerView.Adapter<MainActivity.PostsViewHolder>() {
            @NonNull
            public MainActivity.PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
                return new MainActivity.PostsViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_post_layout, parent, false));
            }
            public void onBindViewHolder(@NonNull MainActivity.PostsViewHolder holder, int position) {
                String[] entry = entries[position];
                holder.setFullname(entry[0]);
                holder.setTime(entry[1]);
                holder.setPost(entry[2]);
                java.util.ArrayList<String> photos = samplePhotos.get(position);
                PostPhotos.bindFeed(holder.itemView, photos);
                bindReactions(holder.itemView, entry[2]);
                holder.itemView.setOnClickListener(v -> startActivity(
                        new Intent(DesignPreviewActivity.this, DesignPreviewActivity.class)
                                .putExtra("screen", "reader").putExtra("entry", entry[2]).putExtra("author", entry[0]).putStringArrayListExtra("imageUrls", photos)));
            }
            public int getItemCount() { return entries.length; }
        });
    }

    private void openEditor() {
        startActivity(new Intent(this, DesignPreviewActivity.class).putExtra("screen", "editor"));
    }

    @Override protected void onResume() {
        super.onResume();
        RecyclerView list = findViewById(R.id.all_users_post_list);
        if (list != null && list.getAdapter() != null) list.getAdapter().notifyDataSetChanged();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        if (photoEditor != null) photoEditor.save(state);
        super.onSaveInstanceState(state);
    }
}
