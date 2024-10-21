package top.kikt.imagescanner.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by JET_HU first on 2024/10/18
 * android (top.kikt.imagescanner.util)
 * Copyright (c) 2024
 * Version: 1.0
 * qq:446902326
 * wx:jet201515
 */
public class PathUtil {

    private UserFileUtils fileUtils;
    private Activity mActivity;
//    private ExecutorService executor = Executors.newSingleThreadExecutor();;
    public PathUtil(Activity mActivity){
        fileUtils = new  UserFileUtils();
//        executor = Executors.newSingleThreadExecutor();
        this.mActivity = mActivity;
    }
    @NonNull
    public ArrayList<String> handleChooseMultiImageResult(int resultCode, Intent intent) {
//         Runnable handlerRunnable;
        if (resultCode == Activity.RESULT_OK && intent != null) {
            ArrayList<MediaPath> paths = getPathsFromIntent(intent, false);
            if (paths == null) {
              paths = new ArrayList<>();
            }
            return handleMediaResult(paths);
        }
        return new ArrayList<String>();
    }
    private ArrayList<MediaPath> getPathsFromIntent(@NonNull Intent data, boolean includeMimeType) {
        ArrayList<MediaPath> paths = new ArrayList<>();
        Uri uri = data.getData();
        // On several pre-Android 13 devices using Android Photo Picker, the Uri from getData() could
        // be null.
        if (uri == null) {
            ClipData clipData = data.getClipData();
            // If data.getData() and data.getClipData() are both null, we are in an error state. By
            // convention we return null from here, and then finish with an error from the corresponding
            // handler.
            if (clipData == null) {
                return null;
            }
            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                uri = data.getClipData().getItemAt(i).getUri();
                // Same error state as above.
                if (uri == null) {
                    return null;
                }
                String path = fileUtils.getPathFromUri(mActivity, uri);
                // Again, same error state as above.
                if (path == null) {
                    return null;
                }
                String mimeType = includeMimeType ? mActivity.getContentResolver().getType(uri) : null;
                paths.add(new MediaPath(path, mimeType));
            }
        } else {
            String path = fileUtils.getPathFromUri(mActivity, uri);
            if (path == null) {
                return null;
            }
            paths.add(new MediaPath(path, null));
        }
        return paths;
    }

    private ArrayList<String> handleMediaResult( ArrayList<MediaPath> paths) {
       /* ImageSelectionOptions localImageOptions = null;
        synchronized (pendingCallStateLock) {
            if (pendingCallState != null) {
                localImageOptions = pendingCallState.imageOptions;
            }
        }

        ArrayList<String> finalPaths = new ArrayList<>();
        if (localImageOptions != null) {
            for (int i = 0; i < paths.size(); i++) {
                MediaPath path = paths.get(i);
                String finalPath = path.path;
                if (path.mimeType == null || !path.mimeType.startsWith("video/")) {
                    finalPath = getResizedImagePath(path.path, localImageOptions);
                }
                finalPaths.add(finalPath);
            }
            finishWithListSuccess(finalPaths);
        } else {
            for (int i = 0; i < paths.size(); i++) {
                finalPaths.add(paths.get(i).path);
            }
            finishWithListSuccess(finalPaths);
        }*/
        ArrayList<String> finalPaths = new ArrayList<String>();
        for (int i = 0; i < paths.size(); i++) {
            MediaPath path = paths.get(i);
            String finalPath = path.path;
            if (path.mimeType == null || !path.mimeType.startsWith("video/")) {
                //finalPath = getResizedImagePath(path.path, localImageOptions);
            }
            finalPaths.add(finalPath);
        }

        return finalPaths;
    }

    public class MediaPath {
        public MediaPath(@NonNull String path, @Nullable String mimeType) {
            this.path = path;
            this.mimeType = mimeType;
        }

        final String path;
        final String mimeType;

        public @NonNull String getPath() {
            return path;
        }

        public @Nullable String getMimeType() {
            return mimeType;
        }
    }
}
