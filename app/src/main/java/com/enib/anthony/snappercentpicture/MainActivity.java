package com.enib.anthony.snappercentpicture;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;


public class MainActivity extends Activity {
    private Camera mCamera = null;
    private CameraView mCameraView = null;
    private ImageView percentView = null;
    private ImageView percentFilter = null;
    private int numberCamera = Camera.getNumberOfCameras();
    private int currentCamera = 0;
    private ImageButton imgClose = null;
    private ImageButton imgChangeCamera = null;
    private ImageButton imgTakePicture = null;
    private ImageButton imgSend = null;
    private ImageButton imgDownload = null;
    private ImageButton imgFilter = null;
    private ImageButton imgPercent = null;
    private FrameLayout camera_view = null;
    private PhotoHandler photoHandler = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera_view = (FrameLayout)findViewById(R.id.camera_view);
        camera_view.setDrawingCacheEnabled(true);
        camera_view.buildDrawingCache();
        percentView = new ImageView(this);
        percentView.setImageResource(R.drawable.percent);
        camera_view.addView(percentView);
        percentView.setVisibility(View.INVISIBLE);

        if (numberCamera>0){
            mCamera = Camera.open(currentCamera);
            if(mCamera != null) {
                mCameraView = new CameraView(this, mCamera);//create a SurfaceView to show camera data
                camera_view.addView(mCameraView);//add the SurfaceView to the layout
            }
        }
        else{
            percentIsComing();
        }

        //btn to percent the application
        imgClose = (ImageButton)findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoHandler.deletePicture();
                mCameraView.surfaceChanged(null,0,0,0);
                takenPictureState();

            }
        });

        //btn to change camera
        imgChangeCamera = (ImageButton)findViewById(R.id.OtherCamera);
        imgChangeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takenPictureState();
                changeCamera();
            }
        });

        imgTakePicture = (ImageButton)findViewById(R.id.imgTakePicture);
        imgTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pictureTakenState();
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        pictureTakenState();                    }
                });
                photoHandler = new PhotoHandler(getApplicationContext());
                mCamera.takePicture(null, null,photoHandler);
            }
        });

        imgSend = (ImageButton)findViewById(R.id.imgSend);
        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(photoHandler.sendPicture(camera_view.getDrawingCache()));
                camera_view.destroyDrawingCache();
            }
        });

        imgDownload = (ImageButton)findViewById(R.id.imgDownload);
        imgDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoHandler.savePicture(camera_view.getDrawingCache());
            }
        });

        imgFilter = (ImageButton)findViewById(R.id.imgFilter);
        imgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                percentFilter();
            }
        });

        imgPercent = (ImageButton)findViewById(R.id.imgPercent);
        imgPercent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                percentIsComing();
            }
        });

        camera_view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        takenPictureState();
    }

    protected void percentIsComing(){
        percentState();
        percentView.setVisibility(View.VISIBLE);
        mCameraView.setVisibility(View.INVISIBLE);

    }

    protected void percentFilter(){
        percentFilter = new ImageView(this);
        percentFilter.setImageResource(R.drawable.test);
        camera_view.addView(percentFilter);

    }

    protected void changeCamera(){
        if (currentCamera == -1){
            currentCamera++;
            mCamera = Camera.open(currentCamera);
            mCameraView.setmCamera(mCamera);
            mCameraView.setVisibility(View.VISIBLE);
            percentView.setVisibility(View.INVISIBLE);
            return;
        }
        if (currentCamera<(numberCamera-1)){
            currentCamera++;
            mCamera.release();
            mCamera = Camera.open(currentCamera);
            mCameraView.setmCamera(mCamera);
        }
        else{
            currentCamera = -1;
            percentIsComing();
        }
    }

    protected void percentState(){
        imgClose.setVisibility(ImageButton.INVISIBLE);
        imgTakePicture.setVisibility(ImageButton.INVISIBLE);
        imgDownload.setVisibility(ImageButton.INVISIBLE);
        imgFilter.setVisibility(ImageButton.INVISIBLE);
        mCameraView.setVisibility(View.INVISIBLE);
    }

    protected void pictureTakenState(){
        imgChangeCamera.setVisibility(ImageButton.INVISIBLE);
        imgSend.setVisibility(ImageButton.VISIBLE);
        imgClose.setVisibility(ImageButton.VISIBLE);
        imgTakePicture.setVisibility(View.INVISIBLE);
        imgDownload.setVisibility(ImageButton.VISIBLE);
        imgFilter.setVisibility(ImageButton.VISIBLE);
    }

    protected void takenPictureState(){
        if (percentFilter != null) {
            percentFilter.setVisibility(View.INVISIBLE);
        }
        imgChangeCamera.setVisibility(ImageButton.VISIBLE);
        imgSend.setVisibility(ImageButton.INVISIBLE);
        imgClose.setVisibility(ImageButton.INVISIBLE);
        imgTakePicture.setVisibility(ImageButton.VISIBLE);
        imgDownload.setVisibility(ImageButton.INVISIBLE);
        imgFilter.setVisibility(ImageButton.INVISIBLE);
    }

}
