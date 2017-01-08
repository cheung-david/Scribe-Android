package com.dc.scribe.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dc.scribe.R;
import com.kosalgeek.android.photoutil.CameraPhoto;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CameraActivity extends AppCompatActivity {
    private ImageView iconCamera, iconGallery, iconUpload, imagePreview;
    private ImageView emptyView;
    private CameraPhoto cameraPhoto;
    private GalleryPhoto galleryPhoto;
    private Bitmap imageToUpload;
    private TextView uploadText;
    private LinearLayout uploadLayout;
    private ProgressDialog progDialog = null;
    final int CAMERA_REQUEST = 13371;
    final int GALLERY_REQUEST = 13372;
    private final String TAG = this.getClass()  .getName();


    public String generateFilename(){
        String timeStamp = (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date());
        String charBank = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 15; i++){
            sb.append(charBank.charAt((int)(Math.random() * 36) % 36));
        }
        sb.append(timeStamp);
        sb.append(".jpg");
        return sb.toString();
    }

    public void prepareUpload() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String imageFileName = generateFilename();
                    //create a file to write bitmap data
                    File f = new File(getBaseContext().getCacheDir(), imageFileName);
                    f.createNewFile();

                    //Convert bitmap to byte array
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    imageToUpload.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                    byte[] bitmapdata = bos.toByteArray();

                    //write the bytes in file
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();
                    uploadFile(f);
                } catch(Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progDialog.dismiss();
                            progDialog = null;
                            Toast.makeText(getApplicationContext(),
                                    "Sorry, the image could not be uploaded. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
                    Log.d("Error:", "Error uploading in Camera Activity" + e);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraPhoto = new CameraPhoto(getApplicationContext());
        galleryPhoto = new GalleryPhoto(getApplicationContext());

        uploadText = (TextView)findViewById(R.id.uploadText);
        iconCamera = (ImageView)findViewById(R.id.camera_icon);
        iconGallery = (ImageView)findViewById(R.id.camera_gallery_icon);
        iconUpload = (ImageView)findViewById(R.id.camera_upload_icon);
        imagePreview = (ImageView)findViewById(R.id.camera_image_preview);
        emptyView = (ImageView)findViewById(R.id.camera_empty_view);
        uploadLayout = (LinearLayout)findViewById(R.id.upload_layout);
        emptyView.setVisibility(View.VISIBLE);

        iconCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivityForResult(cameraPhoto.takePhotoIntent(), CAMERA_REQUEST);
                    cameraPhoto.addToGallery();
                } catch(IOException e) {
                    Log.d(TAG, "err:" + e);
                    Toast.makeText(getApplicationContext(),
                            "Something went wrong while loading the camera", Toast.LENGTH_SHORT).show();
                }

            }
        });

        iconGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(galleryPhoto.openGalleryIntent(), GALLERY_REQUEST);
            }
        });

        uploadLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageToUpload != null) {
                    if(progDialog == null) {
                        progDialog = ProgressDialog.show(CameraActivity.this,
                                "Please wait",
                                "Uploading image...", true);
                    }
                    prepareUpload();
                    //String timeStamp = (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date());
                    //String imageFileName = "JPEG_" + timeStamp + "_";
                    //File file = imageToUpload.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Nothing to upload.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivityForResult(cameraPhoto.takePhotoIntent(), CAMERA_REQUEST);
                    cameraPhoto.addToGallery();
                } catch(IOException e) {
                    Log.d(TAG, "err:" + e);
                    Toast.makeText(getApplicationContext(),
                            "Something went wrong while loading the camera", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public Boolean uploadFile(File file) {
        try {
            final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upload", file.getName(),
                            RequestBody.create(MEDIA_TYPE_JPEG, file))
                    //.addFormDataPart("some-field", "some-value")
                    .build();

            Request request = new Request.Builder()
                    .header("authorization", MainActivity.getToken())
                    .url(MainActivity.getURL_BASE() + "/feed")
                    .post(requestBody)
                    .build();

            MainActivity.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progDialog.dismiss();
                            progDialog = null;
                            Toast.makeText(getApplicationContext(),
                                    "Sorry, the image could not be uploaded. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progDialog.dismiss();
                            progDialog = null;
                            Toast.makeText(getApplicationContext(),
                                    "Image successfully uploaded!", Toast.LENGTH_LONG).show();
                            imageToUpload = null;
                            imagePreview.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        }
                    });
                    response.body().close();
                }
            });
            return true;
        } catch(Exception e) {
            Log.d("Error:", "Error uploading in Camera Activity");
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == CAMERA_REQUEST) {
                String photoPath = cameraPhoto.getPhotoPath();
                try {
                    imageToUpload = ImageLoader.init().from(photoPath).requestSize(800, 800).getBitmap();
                    imagePreview.setImageBitmap(imageToUpload);
                    imagePreview.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    // Scale image back up
                    imageToUpload = ImageLoader.init().from(photoPath).requestSize(1024, 1024).getBitmap();
                    Log.d(TAG, photoPath);
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(),
                            "Something went wrong while loading the photo", Toast.LENGTH_SHORT).show();
                }

            }
            else if(requestCode == GALLERY_REQUEST) {
                Uri url = data.getData();
                galleryPhoto.setPhotoUri(url);
                String photoPath = galleryPhoto.getPath();
                try {
                    imageToUpload = ImageLoader.init().from(photoPath).requestSize(800, 800).getBitmap();
                    imagePreview.setImageBitmap(imageToUpload);
                    imagePreview.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    imageToUpload = ImageLoader.init().from(photoPath).requestSize(1024, 1024).getBitmap();
                    Log.d(TAG, photoPath);
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(),
                            "Something went wrong while loading the photo", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }
}
