#include <jni.h>
#include <string>

#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/features2d.hpp>
#include <opencv2/objdetect/aruco_detector.hpp>
#include <opencv2/objdetect.hpp>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <vector>

using namespace std;
using namespace cv;

// Native APIs
extern "C" {
    JNIEXPORT void JNICALL Java_open_vision_MainActivity_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba){
        Mat& mGr  = *(Mat*)addrGray;
        Mat& mRgb = *(Mat*)addrRgba;
        vector<KeyPoint> v;

        Ptr<FeatureDetector> detector = FastFeatureDetector::create(50);
        detector->detect(mGr, v);
        for( unsigned int i = 0; i < v.size(); i++ ) {
            const KeyPoint& kp = v[i];
            circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
        }
    }

    JNIEXPORT void JNICALL Java_open_vision_MainActivity_GraphVision(JNIEnv*, jobject, jlong addrRgba, jlong addrGray){
        Mat& mRgba = *(Mat*)addrRgba;
        Mat& mGray = *(Mat*)addrGray;
        Mat edges;
        Canny(mGray, edges, 80, 100);

        vector<vector<Point>> contours;
        findContours(edges, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        drawContours(mRgba, contours, -1, Scalar(0, 255,0),2);
    }

    JNIEXPORT void JNICALL Java_open_vision_MainActivity_Transparent3DView(JNIEnv*, jobject, jlong addrRgba, jlong addrLastFrame) {
        Mat &mRgba = *(Mat *) addrRgba;
        Mat& mLastFrame = *(Mat*)addrLastFrame;
        if(mLastFrame.empty()) return;
        if(mRgba.rows != mLastFrame.rows || mRgba.cols != mLastFrame.cols) return;
        double alpha = 0.4;
        addWeighted(mRgba, alpha, mLastFrame, 1.0-alpha, 0.1, mLastFrame);
    }

    JNIEXPORT void JNICALL Java_open_vision_MainActivity_NightVision(JNIEnv*, jobject, jlong addrGray, jlong addrRgba) {
        Mat &mGray = *(Mat *) addrGray;
        Mat &mRgba = *(Mat *) addrRgba;
        // Contrast Enhancement
        // CLAHE is better than global for night scenes.
        Ptr<CLAHE> clahe = createCLAHE(2.0, Size(8,8));
        clahe->apply(mGray, mGray);
        // Edge detection
        Mat edges;
        //Smooth to reduce noise
        GaussianBlur(mGray, mGray, Size(3,3),0);
        Canny(mGray, edges, 50, 150);
        // Blend or Overlay edges for night vision look
        // Convert gray to color to allow green overlay
        Mat colorEffect;
        cvtColor(mGray, colorEffect, COLOR_GRAY2RGBA);
        //Apply green tint
        vector<Mat> channels(4);
        split(colorEffect, channels);
        channels[0] = channels[0]*0.5; //Reduce Red
        channels[1] = channels[1]*1.0; //Keep Green High
        channels[2] = channels[2]*0.5; //Reduce Blue
        // Add white/light-green edges
        colorEffect.setTo(Scalar(100,255,100,255), edges);
        colorEffect.copyTo(mRgba);
    }

    JNIEXPORT void JNICALL Java_open_vision_MainActivity_ArucoDetector(JNIEnv*, jobject, jlong addrGray, jlong addrRgba) {
        Mat &mGray = *(Mat *) addrGray;
        Mat &mRgba = *(Mat *) addrRgba;
        //String gg = aruco::getPredefinedDictionary(aruco::DICT_4X4_50);
    }
}
