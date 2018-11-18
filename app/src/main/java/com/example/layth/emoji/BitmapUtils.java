package com.example.layth.emoji;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.view.Display;

import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BitmapUtils {
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";

    static Bitmap resamplePic (Context context,String imagePath){

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);

        int targetH = metrics.heightPixels;
        int targetW = metrics.widthPixels;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

         return BitmapFactory.decodeFile(imagePath);
    }

    static File createTempImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_"+ timeStamp + "_";
        File storageDir = context.getExternalCacheDir();

        return File.createTempFile(
                imageFileName, /* Prefix*/
                ".jpg",  /* Suffix*/
                storageDir   /*Directory*/
        );
    }
    static boolean deleteImageFile(Context context,String imagePath){
        File imageFile = new File(imagePath);

        boolean deleted = imageFile.delete();

        if(!deleted){
            String errorMessage = context.getString(R.string.error);
            Toast.makeText(context,errorMessage,Toast.LENGTH_SHORT).show();
        }
        return deleted;
    }
    private static void galleryAddPic(Context context, String imagePath){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    static String saveImage(Context context,Bitmap image){
        String savedImagePath = null;

        String timeStamp = new  SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_"+timeStamp+".jpg";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        +"/Emoji");
        boolean success = true;
        if(!storageDir.exists()){
            success = storageDir.mkdir();
        }
        if(success){
            File imageFile = new File(storageDir,imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG,100,fOut);
                fOut.close();
            }catch (Exception e){
                e.printStackTrace();
            }

            galleryAddPic(context,savedImagePath);

            String savedMessage = context.getString(R.string.saved_message, savedImagePath);
            Toast.makeText(context,savedMessage,Toast.LENGTH_SHORT).show();
        }
        return savedImagePath;
    }

    static void  shareImage (Context context, String imagePath){
        File imageFile = new  File(imagePath);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri photoUri = FileProvider.getUriForFile(context,FILE_PROVIDER_AUTHORITY,imageFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM,photoUri);
        context.startActivity(shareIntent);
    }
}
