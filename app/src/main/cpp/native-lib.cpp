#include <jni.h>
#include <string>

#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/features2d.hpp>

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

    JNIEXPORT void JNICALL Java_open_vision_MainActivity_GraphVision(JNIEnv*, jobject, jlong addrRgba){
        Mat& mRgb = *(Mat*)addrRgba;

    }

    JNIEXPORT void JNICALL Java_open_vision_MainActivity_Transparent3DView(JNIEnv*, jobject, jlong addrGray, jlong addrRgba) {
        Mat &mGr = *(Mat *) addrGray;
        Mat &mRgb = *(Mat *) addrRgba;
        vector<KeyPoint> v;
    }
}