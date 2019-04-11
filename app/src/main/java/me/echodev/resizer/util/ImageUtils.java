package me.echodev.resizer.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.media.ExifInterface;

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
        Matrix matrix = getExifRotationMatrix(sourceImage);
        return Bitmap.createBitmap(bitmap, 0, 0, targetWidth, targetHeight, matrix, true);
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
}
