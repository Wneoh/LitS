package com.example.android.lits;

import java.util.Map;

/** Stable database keys are separate from the displayed emoji. */
public final class PostReactions {
    public static final String[] KEYS = {"love", "like", "laugh", "wow", "sad"};
    public static final String[] EMOJI = {"❤️", "👍", "😂", "😮", "😢"};
    public static final String[] LABELS = {"Love", "Like", "Laugh", "Wow", "Sad"};
    private PostReactions() {}

    public static int count(Map<String, String> reactions, String key) {
        int count = 0;
        if (reactions != null) for (String reaction : reactions.values()) {
            if (key.equals(reaction)) count++;
        }
        return count;
    }

    public static String toggle(String current, String selected) {
        for (String key : KEYS) if (key.equals(selected)) {
            return selected.equals(current) ? null : selected;
        }
        throw new IllegalArgumentException("Unknown reaction");
    }
}
