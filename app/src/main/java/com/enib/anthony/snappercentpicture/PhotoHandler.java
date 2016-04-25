package com.enib.anthony.snappercentpicture;

/**
 * Created by anthony on 20/04/2016.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class PhotoHandler implements PictureCallback {

    private final Context context;
    private String path ;
    File pictureFile = null;
    private byte[] data;

    public PhotoHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        this.data = data;
    }

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "SnapPercent");
    }

    public void deletePicture(){
        if(path != null){
            pictureFile = new File(path);
            pictureFile.delete();
            Toast.makeText(context, "Image deleted:" + path,Toast.LENGTH_LONG).show();
        }
    }

    public void savePicture(Bitmap picture){

        path = getPath();

        pictureFile = new File(path);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            Toast.makeText(context, "New Image saved:" + path,
                    Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            Toast.makeText(context, "Image could not be saved.",
                    Toast.LENGTH_LONG).show();
        }
    }

    public Intent sendPicture(Bitmap picture){
        path = getPath();
        pictureFile = new File(path);
        Intent i = new Intent(Intent.ACTION_SEND);
        try {
            FileOutputStream output = new FileOutputStream(path);
            picture.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.close();
            i.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(pictureFile));
            i.setType("image/bitmap");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    public String getPath(){

        File pictureFileDir = getDir();
        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Toast.makeText(context, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return "";

        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "SnaPercent_" + date + ".jpg";
        return pictureFileDir.getPath() + File.separator + photoFile;
    }
}
