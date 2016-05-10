package com.example.opencvtest;

import java.io.File;
import java.util.List;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.PeopleDet;
import com.tzutalin.dlib.VisionDetRet;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private Button btn;
	private ImageView img;
	Bitmap srcBitmap;

	public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		bm.recycle();
		return resizedBitmap;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		img = (ImageView) findViewById(R.id.imageView);
		btn = (Button) findViewById(R.id.btn);
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lena);

		srcBitmap = getResizedBitmap(bitmap, 1000, 1000);

		img.setImageBitmap(srcBitmap);

		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				callFaceLandmark();
			}
		});

	}

	private void callFaceLandmark() {

		new Thread(new Runnable() {
			public void run() {
				final String targetPath = Constants.getFaceShapeModelPath();
				if (!new File(targetPath).exists()) {
					FileUtils.copyFileFromRawToOthers(getApplicationContext(), R.raw.shape_predictor_68_face_landmarks,
							targetPath);
				}
				final String svmPath = Constants.getSVMModelPath();
				FileUtils.copyFileFromRawToOthers(getApplicationContext(), R.raw.person, svmPath);

			
				PeopleDet peopleDet = new PeopleDet();
				List<VisionDetRet> faceList = peopleDet.detBitmapFace(srcBitmap);
		
				Log.d("main", "face is " + faceList.size());
				Message message=new Message();
				message.obj=faceList;
				mHandler.sendMessage(message);

			}
		}).start();

	}
	
	private Handler mHandler=new Handler(){
		public void handleMessage(Message msg){
			List<VisionDetRet> face=(List<VisionDetRet>) msg.obj;
			Log.d("main", "start draw");
			BitmapDrawable image=drawRect(face);
			img.setImageDrawable(image);
		}
	};
	
	
	
	private BitmapDrawable drawRect(List<VisionDetRet> results) {
        
        android.graphics.Bitmap.Config bitmapConfig = srcBitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        srcBitmap = srcBitmap.copy(bitmapConfig, true);
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        // By ratio scale
        float aspectRatio = srcBitmap.getWidth() / (float) srcBitmap.getHeight();

        final int MAX_SIZE = 1000;
        int newWidth = MAX_SIZE;
        int newHeight = MAX_SIZE;
        float resizeRatio = 1;
        newHeight = Math.round(newWidth / aspectRatio);
        if (srcBitmap.getWidth() > MAX_SIZE && srcBitmap.getHeight() > MAX_SIZE) {
            Log.d("main", "Resize Bitmap");
            srcBitmap = getResizedBitmap(srcBitmap, newWidth, newHeight);
            resizeRatio = (float) srcBitmap.getWidth() / (float) width;
            Log.d("main", "resizeRatio " + resizeRatio);
        }

        // Create canvas to draw
        Canvas canvas = new Canvas(srcBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        // Loop result list
        for (VisionDetRet ret : results) {
            Rect bounds = new Rect();
            bounds.left = (int) (ret.getLeft() * resizeRatio);
            bounds.top = (int) (ret.getTop() * resizeRatio);
            bounds.right = (int) (ret.getRight() * resizeRatio);
            bounds.bottom = (int) (ret.getBottom() * resizeRatio);

            canvas.drawRect(bounds, paint);

            String label = ret.getLabel();
            // Draw face landmarks if exists.The format looks like face_landmarks 1,1:50,50,:...
            Log.d("main", "drawRect: label->" + label);
            if (label.startsWith("face_landmarks ")) {
                String[] landmarkStrs = label.replaceFirst("face_landmarks ", "").split(":");
                for (String landmarkStr : landmarkStrs) {
                    String[] xyStrs = landmarkStr.split(",");
                    int pointX = Integer.parseInt(xyStrs[0]);
                    int pointY = Integer.parseInt(xyStrs[1]);
                    pointX = (int) (pointX * resizeRatio);
                    pointY = (int) (pointY * resizeRatio);
                    canvas.drawCircle(pointX, pointY, 2, paint);
                }
            }
        }

        return new BitmapDrawable(getResources(), srcBitmap);
    }
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
