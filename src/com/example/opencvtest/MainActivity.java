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
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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

		srcBitmap = getResizedBitmap(bitmap, 500, 500);

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

				Log.d("main", "people det");

				Log.d("main", "start face landmark");
				int w = srcBitmap.getWidth(), h = srcBitmap.getHeight();
				Log.d("main", "width" + w + "height" + h);
				int[] pix = new int[w * h];
				srcBitmap.getPixels(pix, 0, w, 0, 0, w, h);
				Log.d("main", "start people det");
				PeopleDet peopleDet = new PeopleDet();
				Log.d("main", "start people initial");
				List<VisionDetRet> faceList = peopleDet.detBitmapFace(pix, w, h);
				Log.d("main", "people detfacelist");
				Log.d("main", "face is " + faceList.size());

			}
		}).start();

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
