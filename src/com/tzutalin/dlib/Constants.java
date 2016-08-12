package com.tzutalin.dlib;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import com.example.opencvtest.MyApplication;

/**
 * Created by darrenl on 2016/4/22.
 */
public final class Constants {
   
    /**
     * getFaceShapeModelPath
     * @return default face shape model path
     */
    public static String getFaceShapeModelPath() {
      
    	File cascadeDir = MyApplication.getContext().getDir("landmark", Context.MODE_PRIVATE);
		File mLandmarkFile = new File(cascadeDir, "shape_predictor_68_face_landmarks.dat");
    	String targetPath=mLandmarkFile.getAbsolutePath();
    	
    	Log.d("main", "landmark path"+targetPath);
    	return targetPath;
    }
    public static String getSVMModelPath() {
      
     	File cascadeDir = MyApplication.getContext().getDir("landmark", Context.MODE_PRIVATE);
 		File mLandmarkFile = new File(cascadeDir, "person.svm");
     	String targetPath=mLandmarkFile.getAbsolutePath();
     	Log.d("main", "svm path"+targetPath);
     	return targetPath;
     }
    
    
}
