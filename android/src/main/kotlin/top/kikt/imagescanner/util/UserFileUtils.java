package top.kikt.imagescanner.util;

/**
 * Created by JET_HU first on 2024/10/17
 * android (org.pcgy.wifi)
 * Copyright (c) 2024
 * Version: 1.0
 * qq:446902326
 * wx:jet201515
 */

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import io.flutter.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

class UserFileUtils {

    String getPathFromUri(final Context context, final Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            String uuid = UUID.randomUUID().toString();
            uuid = "images";
            File targetDirectory = new File(context.getCacheDir(), uuid);
            targetDirectory.mkdir();
            // TODO(SynSzakala) according to the docs, `deleteOnExit` does not work reliably on Android; we should preferably
            //  just clear the picked files after the app startup.
//            targetDirectory.deleteOnExit();
            String fileName = getImageName(context, uri);
            String extension = getImageExtension(context, uri);

            if (fileName == null) {
                Log.w("FileUtils", "Cannot get file name for " + uri);
                if (extension == null) extension = ".jpg";
                fileName = "image_picker" + extension;
            } else if (extension != null) {
                fileName = getBaseName(fileName) + extension;
            }
            File file = new File(targetDirectory, fileName);
            try (OutputStream outputStream = new FileOutputStream(file)) {
                copy(inputStream, outputStream);
                return file.getPath();
            }
        } catch (IOException e) {
            // If closing the output stream fails, we cannot be sure that the
            // target file was written in full. Flushing the stream merely moves
            // the bytes into the OS, not necessarily to the file.
            return null;
        } catch (SecurityException e) {
            // Calling `ContentResolver#openInputStream()` has been reported to throw a
            // `SecurityException` on some devices in certain circumstances. Instead of crashing, we
            // return `null`.
            //
            // See https://github.com/flutter/flutter/issues/100025 for more details.
            return null;
        }
    }

    /** @return extension of image with dot, or null if it's empty. */
    private static String getImageExtension(Context context, Uri uriImage) {
        String extension;

        try {
            if (uriImage.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                final MimeTypeMap mime = MimeTypeMap.getSingleton();
                extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uriImage));
            } else {
                extension =
                        MimeTypeMap.getFileExtensionFromUrl(
                                Uri.fromFile(new File(uriImage.getPath())).toString());
            }
        } catch (Exception e) {
            return null;
        }

        if (extension == null || extension.isEmpty()) {
            return null;
        }

        return "." + extension;
    }

    /** @return name of the image provided by ContentResolver; this may be null. */
    private static String getImageName(Context context, Uri uriImage) {
        try (Cursor cursor = queryImageName(context, uriImage)) {
            if (cursor == null || !cursor.moveToFirst() || cursor.getColumnCount() < 1) return null;
            return cursor.getString(0);
        }
    }

    private static Cursor queryImageName(Context context, Uri uriImage) {
        return context
                .getContentResolver()
                .query(uriImage, new String[] {MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        final byte[] buffer = new byte[4 * 1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
    }

    private static String getBaseName(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex < 0) {
            return fileName;
        }
        // Basename is everything before the last '.'.
        return fileName.substring(0, lastDotIndex);
    }
}