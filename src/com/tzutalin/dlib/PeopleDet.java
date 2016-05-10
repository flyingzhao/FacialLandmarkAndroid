/*
*  Copyright (C) 2015 TzuTaLin
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.tzutalin.dlib;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Tzutalin on 2015/10/20.
 */
public class PeopleDet {
    private static final String TAG = "main";
    protected static boolean sInitialized = false;
    static {
        try {
            System.loadLibrary("people_det");
            jniNativeClassInit();
            sInitialized = true;
            android.util.Log.d("main", "jniNativeClassInit success");
        } catch (UnsatisfiedLinkError e) {
            android.util.Log.d("main", "library not found!");
        }
    }

    protected Context mContext;


    public List<VisionDetRet> detBitmapFace(int[] img,int w,int h) {
        List<VisionDetRet> ret = new ArrayList<VisionDetRet>();
        String landmarkPath = "";
        // If landmark exits , then use it
        if (new File(Constants.getFaceShapeModelPath()).exists()) {
            landmarkPath = Constants.getFaceShapeModelPath();
        }
       Log.d("main", "landmark file "+landmarkPath);
        int size = jniBitmapFaceDect(img, w,h,landmarkPath);
        Log.d("main", "face size"+size);
        for (int i = 0; i != size; i++) {
           VisionDetRet det = new VisionDetRet();
            int success = jniGetDLibHOGFaceRet(det, i);
            if (success >= 0) {
                ret.add(det);
                Log.d("main", det.getLabel());
            }
        }
        return ret;
    }
    
    
    


    private native static void jniNativeClassInit();



    private native int jniGetDLibHOGFaceRet(VisionDetRet det, int index);
    
    //传入Bitmap
    private native int jniBitmapFaceDect(int[] image,int w,int h,String landmarkModelPath);

}
