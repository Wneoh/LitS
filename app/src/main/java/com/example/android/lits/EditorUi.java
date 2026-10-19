package com.example.android.lits;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;

/** Shared presentation behavior for the real editor and the offline design preview. */
public final class EditorUi {
    private EditorUi() {}

    public static void bind(Activity activity) {
        EditText input = activity.findViewById(R.id.para);
        TextView counter = activity.findViewById(R.id.word_count);
        View publish = activity.findViewById(R.id.post_para);
        Runnable update = () -> {
            String text = input.getText().toString().trim();
            int words = text.isEmpty() ? 0 : text.split("\\s+").length;
            counter.setText(words + (words == 1 ? " word" : " words"));
            publish.setEnabled(words > 0 || activity.findViewById(R.id.editor_photo).getTag() != null);
        };
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable text) { update.run(); }
        });
        update.run();
    }
}
