package open.vision;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

// import open.vision.databinding.ActivityMainBinding;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

import android.hardware.SensorManager;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends CameraActivity implements CvCameraViewListener2, OnTouchListener {
    private static final String    TAG = "Vision_Main::Activity";

    private static final int       VIEW_MODE_RGBA     = 0;
    private static final int       VIEW_MODE_GRAY     = 1;
    private static final int       VIEW_MODE_CANNY    = 2;
    private static final int       VIEW_MODE_FEATURES = 5;
    private static final int       VIEW_MODE_GRAPH_VISION = 10;
    private static final int       VIEW_MODE_TRANSPARENT_3D_VIEW = 11;
    private static final int       VIEW_MODE_NIGHT_VISION = 12;
    private static final int       VIEW_MODE_ARUCO_VISION = 13;


    private int                    mViewMode;
    private Mat                    mRgba;
    private Mat                    cleanRgba;
    private Mat                    mIntermediateMat;
    private Mat                    mGray;

    private MenuItem               mItemPreviewRGBA;
    private MenuItem               mItemPreviewGray;
    private MenuItem               mItemPreviewCanny;
    private MenuItem               mItemPreviewFeatures;
    private MenuItem               mItemPreviewGraphVision;
    private MenuItem               mItemPreviewNightVision;
    private MenuItem               mItemPreviewTransparent3dView;
    private MenuItem               mItemPreviewArucoDetectorVision;

    private CameraBridgeViewBase   mOpenCvCameraView;
    private Button captureImage;
    private TextView fpsMeter;
    private int framesProcessed;
    private long prevTime;

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successfully");
        } else {
            Log.e(TAG, "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
            return;
        }

        // Load native library after(!) OpenCV initialization
        System.loadLibrary("vision");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cv_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setOnTouchListener(this);

        captureImage = (Button) findViewById(R.id.take_picture);
        captureImage.setOnClickListener(v -> {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "IMG_" + timestamp + ".jpg";
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, 1);
            }
            //mOpenCvCameraView.takePicture(fileName);
            runOnUiThread(() -> Imgcodecs.imwrite(fileName, mRgba));
            //Imgcodecs.imwrite(fileName, mRgba);
            Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
        });

        fpsMeter  = (TextView) findViewById(R.id.fps_meter);
        fpsMeter.setText("FPS: Init");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewRGBA = menu.add("RGBA");
        mItemPreviewGray = menu.add("Gray");
        mItemPreviewCanny = menu.add("Canny");
        mItemPreviewFeatures = menu.add("Find features");
        mItemPreviewGraphVision = menu.add("Graph Vision");
        mItemPreviewNightVision = menu.add("Night Vision");
        mItemPreviewTransparent3dView = menu.add("Transparent 3D View");
        mItemPreviewArucoDetectorVision = menu.add("Aruco Detector");
        return true;

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.enableView();
            mOpenCvCameraView.setOnTouchListener(MainActivity.this);
        }
    }
    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG,"onTouch event");
        return true;
//        return gestureDetector.onTouchEvent(event);
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        cleanRgba = new Mat(height, width, CvType.CV_8UC4);
        prevTime = System.currentTimeMillis();
        framesProcessed=0;
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
        cleanRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        final int viewMode = mViewMode;
        switch (viewMode) {
            case VIEW_MODE_GRAY:
                // input frame has gray scale format
                Imgproc.cvtColor(inputFrame.gray(), mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case VIEW_MODE_RGBA:
                // input frame has RBGA format
                mRgba = inputFrame.rgba();
                break;
            case VIEW_MODE_CANNY:
                // input frame has gray scale format
                mRgba = inputFrame.rgba();
                Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case VIEW_MODE_FEATURES:
                // Native JNI call to Opencv Findfeatures.
                mRgba = inputFrame.rgba();
                mGray = inputFrame.gray();
                FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
                break;
            case VIEW_MODE_GRAPH_VISION:
                // Native JNI call to Graph Vision.
                mRgba = inputFrame.rgba();
                mGray = inputFrame.gray();
                if(mIntermediateMat== null || mIntermediateMat.empty()){
                    mIntermediateMat = mRgba.clone();
                }
                FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
                Core.addWeighted(mRgba, 1.0, mIntermediateMat, 0.3, 0.0, mRgba);
                mRgba.copyTo(mIntermediateMat); // previous frame for low alpha
                break;
            case VIEW_MODE_TRANSPARENT_3D_VIEW:
                // Native JNI call to Transparent 3D View.
                mRgba = inputFrame.rgba();
                mGray = inputFrame.gray();
                //GraphVision(mRgba.getNativeObjAddr(), mGray.getNativeObjAddr());
                Transparent3DView(mRgba.getNativeObjAddr(), mIntermediateMat.getNativeObjAddr());
                break;
            case VIEW_MODE_NIGHT_VISION:
                // Native JNI call to Night Vision.
                mRgba = inputFrame.rgba();
                mGray = inputFrame.gray();
                NightVision(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
                break;
            case VIEW_MODE_ARUCO_VISION:
                // Native JNI call to Night Vision.
                mRgba = inputFrame.rgba();
                mGray = inputFrame.gray();
                ArucoDetector(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
                break;
        }
        framesProcessed++;
        long currentTime= System.currentTimeMillis();
        long timeDiff = currentTime- prevTime;

        // Update FPS every 1 second
        if(timeDiff >= 1000) {
            String frames = String.valueOf(framesProcessed);
            runOnUiThread(() -> fpsMeter.setText(String.format("FPS: %s", frames)));
            framesProcessed=0;
            prevTime= currentTime;
        }
        return mRgba;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemPreviewRGBA) {
            mViewMode = VIEW_MODE_RGBA;
        } else if (item == mItemPreviewGray) {
            mViewMode = VIEW_MODE_GRAY;
        } else if (item == mItemPreviewCanny) {
            mViewMode = VIEW_MODE_CANNY;
        } else if (item == mItemPreviewFeatures) {
            mViewMode = VIEW_MODE_FEATURES;
        } else if (item == mItemPreviewGraphVision) {
            mViewMode = VIEW_MODE_GRAPH_VISION;
        } else if (item == mItemPreviewTransparent3dView) {
            mViewMode = VIEW_MODE_TRANSPARENT_3D_VIEW;
        } else if (item == mItemPreviewNightVision) {
            mViewMode = VIEW_MODE_NIGHT_VISION;
        } else if (item == mItemPreviewArucoDetectorVision) {
            mViewMode = VIEW_MODE_ARUCO_VISION;
        }
        return true;
    }

    public native void FindFeatures(long matAddrGr, long matAddrRgba);
    public native void GraphVision(long matAddrRgba,long matAddrGr);
    public native void Transparent3DView(long matAddrRgba,long matAddrLastFrame);
    public native void NightVision(long matAddrGr, long matAddrRgba);
    public native void ArucoDetector(long matAddrGr, long matAddrRgba);

}
