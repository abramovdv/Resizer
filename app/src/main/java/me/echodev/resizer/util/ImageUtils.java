package me.echodev.resizer.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.IOException;

/**
 * Created by K.K. Ho on 3/9/2017.
 */

public class ImageUtils {

    public static File getScaledImage(int maxLength, int quality, Bitmap.CompressFormat compressFormat,
                                      String outputDirPath, String outputFilename, File sourceImage) throws IOException {
        File directory = new File(outputDirPath);
        if (!directory.exists()) {
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }

        // Prepare the new file name and path
        String outputFilePath = FileUtils.getOutputFilePath(compressFormat, outputDirPath, outputFilename, sourceImage);

        // Write the resized image to the new file
        Bitmap scaledBitmap = getScaledBitmap(maxLength, sourceImage);
        FileUtils.writeBitmapToFile(scaledBitmap, compressFormat, quality, outputFilePath);

        return new File(outputFilePath);
    }

    public static Bitmap getScaledBitmap(int maxLength, File sourceImage) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(sourceImage.getAbsolutePath(), options);
        Matrix matrix = getExifRotationMatrix(sourceImage);

        // Get the dimensions of the original bitmap
        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;
        float aspectRatio = (float) originalWidth / originalHeight;

        int targetWidth = originalWidth;
        int targetHeight = originalHeight;

        if (originalHeight > maxLength || originalWidth > maxLength) {
            // Calculate the target dimensions
            if (originalWidth > originalHeight) {
                targetWidth = maxLength;
                targetHeight = Math.round(targetWidth / aspectRatio);
            } else {
                aspectRatio = 1 / aspectRatio;
                targetHeight = maxLength;
                targetWidth = Math.round(targetHeight / aspectRatio);
            }

        }
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        return Bitmap.createBitmap(scaled, 0, 0, targetWidth, targetHeight, matrix, true);
    }

    private static Matrix getExifRotationMatrix(File sourceImage) throws IOException {
        ExifInterface exif = new ExifInterface(sourceImage.getAbsolutePath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
        }
        return matrix;
    }

    public static File getScaledImage(Context c, int maxLength, int quality, Bitmap.CompressFormat compressFormat,
                                      String outputDirPath, String outputFilename, Uri sourceImage) throws Exception {
        File directory = new File(outputDirPath);
        if (!directory.exists()) {
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }

        Cursor cursor = c.getContentResolver().query(sourceImage,
                new String[]{MediaStore.Images.Media.DISPLAY_NAME}, null, null, null);

        if (cursor != null) {
            if (cursor.moveToNext()) {
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                String caption = cursor.getString(nameColumn);
                String outputFilePath = FileUtils.getOutputFilePath(compressFormat, outputDirPath, outputFilename, caption);
                Bitmap scaledBitmap = getScaledBitmap(c.getContentResolver(), maxLength, sourceImage);

                if (scaledBitmap != null) {
                    FileUtils.writeBitmapToFile(scaledBitmap, compressFormat, quality, outputFilePath);
                    return new File(outputFilePath);
                }
            }
            cursor.close();
        }
        return null;
    }

    private static Matrix getExifRotationMatrix(ParcelFileDescriptor sourceImage) throws IOException {
        ExifInterface exif = new ExifInterface(sourceImage.getFileDescriptor());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
        }
        return matrix;
    }

    @Nullable
    public static Bitmap getScaledBitmap(@NonNull ContentResolver contentResolver, int maxLength, Uri sourceImage) throws IOException {
        ParcelFileDescriptor descriptor = contentResolver.openFileDescriptor(sourceImage, "r");
        if (descriptor != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor());
            Matrix matrix = getExifRotationMatrix(descriptor);

            // Get the dimensions of the original bitmap
            int originalWidth = bitmap.getWidth();
            int originalHeight = bitmap.getHeight();
            float aspectRatio = (float) originalWidth / originalHeight;

            int targetWidth = originalWidth;
            int targetHeight = originalHeight;

            if (originalHeight > maxLength || originalWidth > maxLength) {
                // Calculate the target dimensions
                if (originalWidth > originalHeight) {
                    targetWidth = maxLength;
                    targetHeight = Math.round(targetWidth / aspectRatio);
                } else {
                    aspectRatio = 1 / aspectRatio;
                    targetHeight = maxLength;
                    targetWidth = Math.round(targetHeight / aspectRatio);
                }

            }
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
            return Bitmap.createBitmap(scaled, 0, 0, targetWidth, targetHeight, matrix, true);
        } else {
            return null;
        }
    }

}
