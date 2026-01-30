# Vision

### `OpenCV and Custom Android Native C++ libraries are used for multiple modes of Camera Vision`

Camera input pixels are processed as a Graph (Grid) and a Breadth-First-Search is performed with a threshold to perform color clumping, edge detection.
Transparent 3D View uses both Camera pixel input and its movement to compute a real-time 3D Mat of detected features.  
This project is built on top of opencv-android-sdk Tutorial2-Mixed Processing. 2 additional View modes and their corresponding custom libraries are introduced to perform Graph Vision and Transparent 3D View.

## Additional documentation

- [Add Native Code to Project](https://developer.android.com/studio/projects/add-native-code.html)
- [JNI tips](https://developer.android.com/ndk/guides/jni-tips#native-libraries)
- [controlling symbol visibility](https://developer.android.com/ndk/guides/symbol-visibility)
