package com.thilakshi.storage_permission_android11;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button button;

    ActivityResultLauncher<String> mGetContent;
    ActivityResultLauncher<Intent> mGetPermission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.btn);
        imageView = findViewById(R.id.ImgV);

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                imageView.setImageURI(result);
                try {
                   saveImageToGallery(result);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
        
        mGetPermission = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()==MainActivity.RESULT_OK){
                    Toast.makeText(getApplicationContext(), "Permission Given In Android 11", Toast.LENGTH_SHORT).show();
                }


            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFile(view);
            }
        });
    }

    private void saveImageToGallery(Uri result) throws IOException {

        OutputStream fos;
        ContentResolver resolver = getContentResolver();
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis()+".jpeg");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+ File.separator+"StorageTutorialFolder");
        Uri imageUri= resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        fos= resolver.openOutputStream(Objects.requireNonNull(imageUri));
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), result);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100,fos);
        Objects.requireNonNull(fos);

    }

    private void selectFile(View view) {

        takePermission();
        mGetContent.launch("image/*");
    }

    private void takepermissions() {

        if(Build.VERSION.SDK_INT== Build.VERSION_CODES.R){
            try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.Default");
                    intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                    mGetPermission.launch(intent);

            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
    else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }

    }

    private boolean isPermissionGranted(){
        if(Build.VERSION.SDK_INT==Build.VERSION_CODES.R){
            return Environment.isExternalStorageManager();
        }
        else {

            int readExternalStoragePermission= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return  readExternalStoragePermission == PackageManager.PERMISSION_GRANTED;
        }
    }

    public  void takePermission(){

        if(isPermissionGranted()){
            Toast.makeText(getApplicationContext(), "Permission Already", Toast.LENGTH_SHORT).show();
        }
        else{
            takepermissions();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            if(requestCode==101){

                boolean readExternalStorage=grantResults[0] ==PackageManager.PERMISSION_GRANTED;
                if (readExternalStorage){
                    Toast.makeText(getApplicationContext(), "Permission Allowed In Android 10 or Below", Toast.LENGTH_SHORT).show();
                }
                else{

                }
            }
        }
    }
}