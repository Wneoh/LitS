# LitS

Native Android social posting app, written in Java with Firebase authentication,
Realtime Database, and profile-photo storage.

## Android build

- Compile/target SDK: Android 16 (API 36); minimum SDK: Android 7.0 (API 24).
- Android Gradle Plugin 8.10.1, Gradle 8.11.1, and Java 17 or 21.
- Use Android Studio Narwhal (2025.1.1) or newer for IDE sync.
- Install Android SDK Platform 36 and Build Tools 35.0.0.
- Set `sdk.dir` in your untracked `local.properties` to your Android SDK location.

On Windows, with `JAVA_HOME` pointing to a compatible JDK:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
adb shell am start -n com.example.android.lits/.MainActivity
```

For a OneDrive checkout, keep generated output outside the synced folder to avoid
file-lock failures. Pass the same property to each build/install/test command:

```powershell
.\gradlew.bat assembleDebug connectedDebugAndroidTest "-PlitsBuildDir=$env:LOCALAPPDATA/LitS/build"
```

The APK is normally at `app/build/outputs/apk/debug/app-debug.apk`, or under
`<litsBuildDir>/app/outputs/apk/debug/` when using the optional output directory.

## Compatibility notes

The UI uses AndroidX, handles system-bar/cutout/keyboard insets, and scrolls the
login, registration, and profile forms on smaller screens. Profile images are
selected through the system document picker without broad storage permissions.

The original Firebase SDKs and FirebaseUI integration are retained, with Jetifier
bridging older libraries to AndroidX. A full Firebase dependency upgrade and
authenticated backend testing remain separate work. The legacy image-cropper
dependency is pinned and retrieved from the JCenter mirror in `build.gradle`.

Device tests cover the unauthenticated UI and do not create accounts or posts.

## Post photos

The composer accepts up to six photos through the system document picker, with
thumbnail selection and individual removal before publishing. Text-only and photo-only
entries are supported. Selected URIs survive normal activity recreation.
Cancel upload (or Back) stops preparation/upload, invalidates late callbacks,
attempts to clean up uploaded files, and returns to the draft. Once the final
database write starts, Close lets the user leave while the save continues;
it does not claim to cancel a write already sent to Firebase.
Images are resized to fit 1600 × 1600 and encoded as JPEG (quality 82) off the UI
thread before upload. Animated images become a still image.

`Posts` stores `imageUrls` and `imagePaths` lists, with fallback to the older
`imageUrl` and `imagePath` fields. Files live at
`Post Images/{authUid}/{pushId}-{index}.jpg` in Firebase Storage. A post is
written after every photo upload succeeds. Failed post writes trigger best-effort
photo cleanup; interrupted processes can leave orphan uploads for later cleanup.
Deleting an entry asks for confirmation, removes its database record and then
attempts to delete its photo. Existing image captions can be edited; replacing
photos after publication is not part of this version.

Cards show compact photo mosaics (up to four tiles, with an overflow count). The reader uses a fixed-height swipe gallery, preserving image aspect
ratio with author/date information. Feed photos open the entry details; tap the
photo in entry details for a full-screen fit view.
The debug feed bundles six free Pexels photos for offline previews. Sources and photographer credits are in PHOTO_CREDITS.md; no sample posts are uploaded.

Live publication is unverified while the Realtime Database is deactivated.
Storage must also be provisioned with rules allowing authenticated reads and
owner-only writes/deletes under `Post Images/{authUid}/{file}`, with JPEG type
and size validation for uploads. Update any post schema validation to accept
the optional image lists (maximum six) and an empty caption when images exist.
No cloud rules were deployed by this change.

## Post reactions

Feed cards and entry details offer Love, Like, Laugh, Wow, and Sad reactions.
Tap a selected reaction again to remove it, or choose another to switch.
`Posts.reactions` is a nullable `Map<String, String>` stored as
`Posts/{postId}/reactions/{authUid}: "love" | "like" | "laugh" | "wow" | "sad"`.
Existing posts need no migration. Counts are derived from the per-user map;
transactions update only the reacting user's value, without overwriting others.
The debug sample feed supports the same interaction in memory, without Firebase writes.

Live syncing requires the deactivated Realtime Database to be restored and its
rules to permit signed-in readers to transact on their own reaction. Merge the
following node under the existing `Posts/$postId` rules (this is a fragment, not
a complete rules file, and has not been deployed):

```json
"reactions": {
  "$uid": {
    ".read": "auth != null",
    ".write": "auth != null && auth.uid === $uid && root.child('Posts').child($postId).child('uid').exists()",
    ".validate": "newData.isString() && (newData.val() === 'love' || newData.val() === 'like' || newData.val() === 'laugh' || newData.val() === 'wow' || newData.val() === 'sad')"
  }
}
```

Existing post-read permission is needed to display totals. Do not grant broad
write access at the database or Posts parent: ancestor write grants override
child restrictions. Keep post author/content permissions separate from reaction
permissions, and verify rules before deployment. Deleting a reaction is allowed;
Realtime Database skips `.validate` for deleted values.

## Appearance

Choose **Classic**, **Forest**, **Sepia**, or **Midnight** from **Appearance** in the
navigation drawer, or **Choose your theme** on the login screen. Themes apply
immediately and are saved on the device. Returning to an already-open screen
refreshes its theme while preserving its normal Android view state.

## Design preview (debug builds only)

The app uses a light neutral/burgundy theme with compact sans-serif typography.
The feed shows author initials, readable entry cards, and a floating Write button;
the editor shows a live word count with Publish in the top bar.
Publishing still requires a signed-in account and a successful Firebase write.

To inspect the feed without an account or network calls:

```powershell
adb shell am start -n com.example.android.lits/.DesignPreviewActivity
```

Tap **Write** to try the editor. Sample entries are labeled, and Publish
only shows a preview message. This activity is excluded from release builds.


## Comments

Post details show the latest 50 comments in chronological order. Signed-in users
with a completed profile can submit a comment of up to 1,000 characters. Empty
comments are disabled. The composer keeps its text on failure and displays a
pending state until the database confirms the write. Preview replies are local,
shared only within the debug app process; no accounts or database writes are made.

Records are stored at `Posts/{postId}/comments/{pushId}` with `uid`, `author`,
`text`, and `createdAt` (Firebase server timestamp). Deleting a post also removes
its comments. Existing posts need no migration. Comment editing, deletion and
moderation controls are not included in this first version.

Before live use, restore the database and merge rules like the following under
`Posts/$postId`. This is a fragment, not a deployable rules file. It has not been
deployed or tested against the unavailable backend. Parent write grants must not
allow arbitrary users to overwrite posts/comments; Firebase grants cascade.

```json
"comments": {
  ".read": "auth != null",
  ".indexOn": ["createdAt"],
  "$commentId": {
    ".write": "auth != null && !data.exists() && newData.child('uid').val() === auth.uid && root.child('Posts').child($postId).child('uid').exists()",
    ".validate": "newData.hasChildren(['uid', 'author', 'text', 'createdAt'])",
    "uid": { ".validate": "newData.isString() && newData.val() === auth.uid" },
    "author": { ".validate": "newData.isString() && newData.val() === root.child('Users').child(auth.uid).child('fullname').val()" },
    "text": { ".validate": "newData.isString() && newData.val().length > 0 && newData.val().length <= 1000 && newData.val().matches(/[\\s\\S]*\\S[\\s\\S]*/)" },
    "createdAt": { ".validate": "newData.isNumber() && newData.val() === now" },
    "$other": { ".validate": false }
  }
}
```
