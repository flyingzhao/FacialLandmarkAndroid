/*
 * jni_people_det.cpp using google-style
 *
 *  Created on: Oct 20, 2015
 *      Author: Tzutalin
 *
 *  Copyright (c) 2015 Tzutalin. All rights reserved.
 */

#include <dlib/image_loader/load_image.h>
#include <dlib/image_processing.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing/render_face_detections.h>
#include <dlib/opencv/cv_image.h>
#include <dlib/image_loader/load_image.h>
#include <glog/logging.h>
#include <jni.h>
#include <memory>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/opencv.hpp>
#include <stdio.h>
#include <string>
#include <vector>
#include <unordered_map>
#include <tr1/unordered_map>



class DLibHOGDetector {
 public:
  DLibHOGDetector(std::string modelPath ="/data/data/com.example.opencvtest/app_landmark/person.svm")
      : mModelPath(modelPath) {
	  LOG(INFO) << "Model Path: " << mModelPath;
  }

  virtual inline int det(std::string path) {
    cv::Mat src_img = cv::imread(path, 1);

    typedef dlib::scan_fhog_pyramid<dlib::pyramid_down<6> > image_scanner_type;
    dlib::object_detector<image_scanner_type> detector;
    dlib::deserialize(mModelPath) >> detector;

    int img_width = src_img.cols;
    int img_height = src_img.rows;
    int im_size_min = MIN(img_width, img_height);
    int im_size_max = MAX(img_width, img_height);

    float scale = float(INPUT_IMG_MIN_SIZE) / float(im_size_min);
    if (scale * im_size_max > INPUT_IMG_MAX_SIZE) {
      scale = (float)INPUT_IMG_MAX_SIZE / (float)im_size_max;
    }

    if (scale != 1.0) {
      cv::Mat outputMat;
      cv::resize(src_img, outputMat, cv::Size(img_width * scale, img_height * scale));
      src_img = outputMat;
    }

    // cv::resize(src_img, src_img, cv::Size(320, 240));
    dlib::cv_image<dlib::bgr_pixel> cimg(src_img);

    double thresh = 0.5;
    std::vector<dlib::rectangle> dets = detector(cimg, thresh);
    return 0;
  }

  inline std::vector<dlib::rectangle> getResult() { return mRets; }

 protected:
  std::vector<dlib::rectangle> mRets;
  std::string mModelPath;
  const int INPUT_IMG_MAX_SIZE = 800;
  const int INPUT_IMG_MIN_SIZE = 600;
};

/*
 * DLib face detect and face feature extractor
 */
class DLibHOGFaceDetector : public DLibHOGDetector {
 private:
  std::string mLandMarkModel;
  dlib::shape_predictor msp;
  std::tr1::unordered_map <int, dlib::full_object_detection> mFaceShapeMap;


 public:
  DLibHOGFaceDetector(std::string landmarkmodel = "")
      : mLandMarkModel(landmarkmodel) {
    if (!mLandMarkModel.empty()) {
      dlib::deserialize(mLandMarkModel) >> msp;
      LOG(INFO) << "Load landmarkmodel from " << mLandMarkModel;
    }
  }



  virtual inline int det(cv::Mat image){

	    LOG(INFO) << "com_tzutalin_dlib_PeopleDet go to det(mat)";
	     dlib::frontal_face_detector detector = dlib::get_frontal_face_detector();

	     cv::cvtColor(image,image,CV_RGBA2RGB);
	     dlib::cv_image<dlib::bgr_pixel> img(image);
	     mRets = detector(img);
	     LOG(INFO) << "Dlib HOG face det size : " << mRets.size();
		 	mFaceShapeMap.clear();

	     if (mRets.size() != 0 && mLandMarkModel.empty() == false) {
	       for (unsigned long j = 0; j < mRets.size(); ++j) {
	         dlib::full_object_detection shape = msp(img, mRets[j]);
	         LOG(INFO) << "face index:" << j << "number of parts: " << shape.num_parts();
	         mFaceShapeMap[j] = shape;
	       }
	     }

	     return mRets.size();
  }

  std::tr1::unordered_map<int, dlib::full_object_detection>& getFaceShapeMap() {
		return mFaceShapeMap;
	}


};


DLibHOGDetector* gDLibHOGDetector = NULL;
DLibHOGFaceDetector* gDLibHOGFaceDetector = NULL;

#ifdef __cplusplus
extern "C" {
#endif

struct VisionDetRetOffsets {
  jfieldID label;
  jfieldID confidence;
  jfieldID left;
  jfieldID top;
  jfieldID right;
  jfieldID bottom;
} gVisionDetRetOffsets;

// ========================================================
// JNI Mapping Methods
// ========================================================
jint JNIEXPORT JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
	LOG(INFO) << "JNI On Load";
  JNIEnv* env = NULL;
  jint result = JNI_ERR;

  if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
	  LOG(FATAL) << "GetEnv failed!";
    return result;
  }

  return JNI_VERSION_1_6;
}

#define DLIB_JNI_METHOD(METHOD_NAME) \
  Java_com_tzutalin_dlib_PeopleDet_##METHOD_NAME

void JNIEXPORT DLIB_JNI_METHOD(jniNativeClassInit)(JNIEnv* _env, jclass _this) {
  jclass detRetClass = _env->FindClass("com/tzutalin/dlib/VisionDetRet");
  gVisionDetRetOffsets.label =
      _env->GetFieldID(detRetClass, "mLabel", "java/lang/String");
  gVisionDetRetOffsets.confidence =
      _env->GetFieldID(detRetClass, "mConfidence", "F");
  gVisionDetRetOffsets.left = _env->GetFieldID(detRetClass, "mLeft", "I");
  gVisionDetRetOffsets.top = _env->GetFieldID(detRetClass, "mTop", "I");
  gVisionDetRetOffsets.right = _env->GetFieldID(detRetClass, "mRight", "I");
  gVisionDetRetOffsets.bottom = _env->GetFieldID(detRetClass, "mBottom", "I");
  LOG(INFO) << "JniNativeClassIni Success";
}


jint JNIEXPORT JNICALL
    DLIB_JNI_METHOD(jniGetDLibHOGFaceRet)(JNIEnv* env, jobject thiz,
					  jobject detRet, jint index) {
  if (gDLibHOGFaceDetector) {
    dlib::rectangle rect = gDLibHOGFaceDetector->getResult()[index];
    env->SetIntField(detRet, gVisionDetRetOffsets.left, rect.left());
    env->SetIntField(detRet, gVisionDetRetOffsets.top, rect.top());
    env->SetIntField(detRet, gVisionDetRetOffsets.right, rect.right());
    env->SetIntField(detRet, gVisionDetRetOffsets.bottom, rect.bottom());
    env->SetFloatField(detRet, gVisionDetRetOffsets.confidence, 0);
    jstring jstr = (jstring)(env->NewStringUTF("face"));
    env->SetObjectField(detRet, gVisionDetRetOffsets.label, (jobject)jstr);

    std::tr1::unordered_map<int, dlib::full_object_detection>& faceShapeMap = gDLibHOGFaceDetector->getFaceShapeMap();
    		if (faceShapeMap.find(index) != faceShapeMap.end()) {
			dlib::full_object_detection shape = faceShapeMap[index];
			std::stringstream ss;
			// If landmarks exists, set label as "face_landmarks "
			if (shape.num_parts() > 0) {
				ss << "face_landmarks ";
			}
		  for (int i = 0 ; i != shape.num_parts(); i++) {
			  int x = shape.part(i).x();
			  int y = shape.part(i).y();
				ss << x << "," << y << ":";
		  }
			// TODO: Workaround. No availe time to better. It should be List<Point>
      jstring jstr = (jstring)(env->NewStringUTF(ss.str().c_str()));
      env->SetObjectField(detRet, gVisionDetRetOffsets.label, (jobject)jstr);
		}
    return JNI_OK;
  }

  return JNI_ERR;
}


//Bitmap face detection
JNIEXPORT jint JNICALL DLIB_JNI_METHOD(jniBitmapFaceDect)
  (JNIEnv *env, jobject thiz, jintArray img,jint w,jint h, jstring landmarkPath){
	 LOG(INFO) << "com_tzutalin_dlib_PeopleDet jniBitmapFaceDect";

	  jint *cbuf;
	   cbuf = env->GetIntArrayElements(img, JNI_FALSE );
	      if (cbuf == NULL) {
	          return 0;
	      }

	  cv::Mat imgData(h, w, CV_8UC4, (unsigned char *) cbuf);
	  const char* landmarkmodel_path = env->GetStringUTFChars(landmarkPath, 0);
	  LOG(INFO) << "new DLibHOGFaceDetector";
	  if (gDLibHOGFaceDetector == NULL){
		  LOG(INFO) << landmarkmodel_path;
	    gDLibHOGFaceDetector = new DLibHOGFaceDetector(landmarkmodel_path);
	  }
	  LOG(INFO) << "com_tzutalin_dlib_PeopleDet start det face";
	  jint size=gDLibHOGFaceDetector->det(imgData);
	  LOG(INFO) << "com_tzutalin_dlib_PeopleDet start det face"+size;
	  env->ReleaseIntArrayElements(img,cbuf,0);
	  env->ReleaseStringUTFChars(landmarkPath, landmarkmodel_path);
	  return size;
}


#ifdef __cplusplus
}
#endif
