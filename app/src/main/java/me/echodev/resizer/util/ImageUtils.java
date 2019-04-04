package me.echodev.resizer.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
            directory.mkdirs();
        }

        // Prepare the new file name and path
        String outputFilePath = FileUtils.getOutputFilePath(compressFormat, outputDirPath, outputFilename, sourceImage);

        // Write the resized image to the new file
        Bitmap scaledBitmap = getScaledBitmap(maxLength, sourceImage);
        FileUtils.writeBitmapToFile(scaledBitmap, compressFormat, quality, outputFilePath);

        return new File(outputFilePath);
    }

    public static Bitmap getScaledBitmap(int maxLength, File sourceImage) {
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


        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
    }
}
