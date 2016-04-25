package com.enib.anthony.snappercentpicture;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;


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

    private CameraSource mCameraSource = null;
    private GraphicOverlay mGraphicOverlay;
    private CameraSourcePreview mPreview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

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
                mCameraView.setVisibility(View.INVISIBLE);
                createCameraSource();
                startCameraSource();
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

    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(currentCamera)
                .setRequestedFps(30.0f)
                .build();
    }

    private void startCameraSource() {

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource);
            } catch (IOException e) {
                Log.e("azerty", "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
            Log.d("New Face","face");
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
            Log.d("Face ID:", String.valueOf(faceId));
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }

}
