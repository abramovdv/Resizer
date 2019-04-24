package ru.resizer.abramovdv.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import me.echodev.resizer.Resizer;

/**
 * This sample app is ugly, sry :)
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static int REQUEST_IMAGE_CAPTURE = 42;
    private static int DEF_QUALITY = 80;

    private static final String TEMPLATE_1 = "%.2f kB (%d x %d)";

    private String path;
    private ImageView normalView;
    private TextView normalSizeTextView;
    private ImageView resizedView;
    private TextView resizedSizeTextView;
    private TextView qualityText;
    private Button makePhoto;
    private SeekBar seekBar;

    private File normalImageFile;
    private File resizedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        normalView = findViewById(R.id.normal_photo);
        resizedView = findViewById(R.id.resized_photo);
        makePhoto = findViewById(R.id.button);

        normalSizeTextView = findViewById(R.id.size_normal_text);
        resizedSizeTextView = findViewById(R.id.size_resized_text);

        qualityText = findViewById(R.id.quality_text);

        normalView.setOnClickListener(this);
        resizedView.setOnClickListener(this);

        seekBar.setProgress(DEF_QUALITY);

        makePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                path = startCameraActivity(MainActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                normalImageFile = new File(path);
                setViews(normalImageFile, normalSizeTextView, normalView);

                String newDirName = normalImageFile.getParent();
                String newFileName = "example_image_file_compressed";

                try {
                    resizedImageFile = new Resizer(MainActivity.this, 3000)
                            .setQuality(seekBar.getProgress())
                            .setOutputFormat(Bitmap.CompressFormat.JPEG)
                            .setOutputDirPath(newDirName)
                            .setOutputFilename(newFileName)
                            .setSourceImage(normalImageFile).getResizedFile();

                    setViews(resizedImageFile, resizedSizeTextView, resizedView);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setViews(File file, TextView textView, ImageView imageView) {
        Bitmap b = getBitmap(file);
        float sizeSize = getSize(file);

        textView.setText(String.format(Locale.ENGLISH, TEMPLATE_1, sizeSize, b.getHeight(), b.getWidth()));
        imageView.setImageBitmap(b);
    }

    public float getSize(File file) {
        long bytes = file.length();
        return bytes / 1024.0f;
    }

    private Bitmap getBitmap(File img) {
        Log.e(TAG, img.getAbsolutePath());
        BitmapFactory.Options options = new BitmapFactory.Options();
        return BitmapFactory.decodeFile(img.getAbsolutePath(), options);
    }

    static String startCameraActivity(Activity activity) {
        String newPath = null;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (activity != null && takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            newPath = getNewPath(activity);
            if (newPath != null) {
                Uri photoURI = getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", new File(newPath));
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    activity.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(activity, "new path is empty", Toast.LENGTH_SHORT).show();
            }
        }
        return newPath;
    }

    @Nullable
    public static Uri getUriForFile(@NonNull Context context, @NonNull String authority, @NonNull File file) {
        if ("Huawei".equalsIgnoreCase(Build.MANUFACTURER) && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            try {
                return FileProvider.getUriForFile(context, authority, file);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.getLocalizedMessage());
                return Uri.fromFile(file);
            }
        } else {
            return FileProvider.getUriForFile(context, authority, file);
        }
    }


    @Nullable
    private static String getNewPath(Context context) {
        try {
            File dir = new File(context.getFilesDir(), "images");
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }
            return dir + "/example_image_file.jpg";
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        return null;
    }

    private void openImageInAnotherApp(Context context, File file) {
        if (file != null) {
            Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Context context = MainActivity.this;
        switch (id) {
            case R.id.normal_photo:
                openImageInAnotherApp(context, normalImageFile);
                break;
            case R.id.resized_photo:
                openImageInAnotherApp(context, resizedImageFile);
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (qualityText != null) {
            qualityText.setText("Quality: " + progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
