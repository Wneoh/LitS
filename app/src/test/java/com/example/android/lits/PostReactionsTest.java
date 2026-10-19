package com.example.android.lits;

import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;

public class PostReactionsTest {
    @Test public void legacyPostsHaveZeroReactions() {
        assertEquals(0, PostReactions.count(new Posts().getReactions(), "love"));
    }
    @Test public void usersCanAddSwitchAndRemoveWithoutChangingOthers() {
        HashMap<String, String> reactions = new HashMap<>();
        reactions.put("other", "love");
        reactions.put("me", PostReactions.toggle(null, "love"));
        assertEquals(2, PostReactions.count(reactions, "love"));
        reactions.put("me", PostReactions.toggle(reactions.get("me"), "wow"));
        assertEquals(1, PostReactions.count(reactions, "love"));
        assertEquals(1, PostReactions.count(reactions, "wow"));
        assertNull(PostReactions.toggle(reactions.get("me"), "wow"));
        assertEquals("love", reactions.get("other"));
    }
    @Test(expected = IllegalArgumentException.class) public void rejectsUnknownKeys() {
        PostReactions.toggle(null, "invalid");
    }
}
