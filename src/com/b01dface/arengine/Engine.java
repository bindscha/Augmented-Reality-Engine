package com.b01dface.arengine;

import com.b01dface.arlo.core.R;
import com.b01dface.arlo.overlays.CalendarAdapter;

import java.util.ArrayList;
import java.util.List;

import com.b01dface.arengine.ARLayout;
import com.b01dface.arengine.CameraPreview;
import com.b01dface.arengine.ARLayout.Mode;
import com.b01dface.arengine.CameraPreview.PreviewState;
import com.b01dface.arengine.overlays.DummyEntity;
import com.b01dface.arengine.overlays.Overlay;
import com.b01dface.arengine.overlays.OverlayCollection;
import com.b01dface.arengine.util.Azimuth;
import com.b01dface.arengine.util.Inclination;
import com.b01dface.arengine.util.Triple;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The <code>Engine<code> is the base <code>Activity</code> for the ARE.
 * It manages the lifecycle and connects the various components together.
 * 
 * @author Laurent Bindschaedler
 */
public class Engine extends Activity {

	public static final int MINIMUM_PAUSE_DELAY = 500;
	
	public static final long HAPTIC_FEEDBACK_VIBRATION_PATTERN[] = new long[]{0, 40};
	
	private static final int ENABLE_GPS_REQUEST = 0;

	private FrameLayout mainLayout_;
	private FrameLayout splashLayout_;
	private FrameLayout cameraLayout_;
	
	private CameraPreview cameraPreview_;
	private ImageView cameraImageView_;
	
	private TextView progressText_;
	
	private ARLayout arLayout_;
	
	private Toast pauseToast_;
	private ImageView toastImage_;
	
	private Dialog gpsDialog_;

	private CustomWhereaboutsManager whereaboutsManager_;
	
	private AddressNotifier addressNotifier_;
	
	private long lastPauseTime_;
	
	private InitThread initThread_;
	private boolean initThreadStarted_;
	
	private class InitThread extends Thread {
		
		private static final long SLEEP_TIME = 50;
		
		@Override
		public void run() {
			// Wait until GPS is enabled
			while(!gpsEnabled()) {
				try {
					Thread.sleep(SLEEP_TIME);
				} catch(InterruptedException e) {
					// ignored
				}
			}
			
			// Display camera and splash
			mainLayout_.post(new Runnable() {
				
				@Override
				public void run() {
					cameraLayout_.setVisibility(View.VISIBLE);
					splashLayout_.setVisibility(View.VISIBLE);
				}
			});
			
			// Step 1 - Preparing camera
			progressText_.post(new Runnable() {
				
				@Override
				public void run() {
					progressText_.setText(R.string.preparing_camera);
				}
			});
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// ignored
			}
			
			// Step 2 - Preparing data structures
			progressText_.post(new Runnable() {
				
				@Override
				public void run() {
					progressText_.setText(R.string.building_data_structures);
				}
			});
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// ignored
			}

			// Step 3 - Loading layout
			progressText_.post(new Runnable() {
				
				@Override
				public void run() {
					progressText_.setText(R.string.loading_layout);
				}
			});
			
			try {
				Thread.sleep(800);
			} catch (InterruptedException e) {
				// ignored
			}
			
			// Step 4 - Registering location
			progressText_.post(new Runnable() {
				
				@Override
				public void run() {
					progressText_.setText(R.string.registering_location);
				}
			});
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignored
			}
			
			// Step 5 - Displaying overlays
			progressText_.post(new Runnable() {
				
				@Override
				public void run() {
					progressText_.setText(R.string.display_overlays);
				}
			});
			
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				// ignored
			}
			
			// Ask UI thread to call onReady()
			mainLayout_.post(new Runnable() {
				
				@Override
				public void run() {
					Engine.this.ready();
				}
				
			});
		}
		
	}
	
	@Override
	public void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		
		ConfigurationManager.contextIs(this);
		
		setContentView(R.layout.main);
					
		mainLayout_ = (FrameLayout) findViewById(R.id.MainLayout);
		
		splashLayout_ = (FrameLayout) findViewById(R.id.SplashLayout);
		splashLayout_.setVisibility(View.INVISIBLE);
		
		cameraLayout_ = (FrameLayout) findViewById(R.id.CameraLayout);
		cameraLayout_.setVisibility(View.INVISIBLE);

		arLayout_ = (ARLayout) findViewById(R.id.ARLayout);
		arLayout_.setVisibility(View.INVISIBLE);
		
		cameraPreview_ = (CameraPreview) findViewById(R.id.CameraPreview);
		
		cameraImageView_ = (ImageView) findViewById(R.id.CameraImageView);
		
		progressText_ = (TextView) findViewById(R.id.ProgressText);
		progressText_.getPaint().setAntiAlias(true);
		
		// Toast initialization
		
		lastPauseTime_ = 0;
		
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_image, (ViewGroup) findViewById(R.id.toastLayout));
		
		toastImage_ = (ImageView) layout.findViewById(R.id.toastImage);
		toastImage_.setImageResource(R.drawable.pause);
		
		pauseToast_ = new Toast(this);
		pauseToast_.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		pauseToast_.setDuration(Toast.LENGTH_SHORT);
		pauseToast_.setView(layout);
		
		// GPS dialog initialization
		
		gpsDialog_ = new Dialog(this);

		gpsDialog_.setContentView(R.layout.gps_dialog);
		gpsDialog_.setTitle(R.string.gps_dialog_title);
		
		gpsDialog_.setCancelable(false);
		
		Button enabledGPSButton = (Button) gpsDialog_.findViewById(R.id.enableGPSButton);
		enabledGPSButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				gpsDialog_.dismiss();
				
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				try {
					startActivityForResult(intent, ENABLE_GPS_REQUEST);
				} catch(ActivityNotFoundException e) {
					// TODO: handle this case
				}
			}
		});
		
		initThread_ = new InitThread();
		initThreadStarted_ = false;
		
		// State variables

		if(_savedInstanceState != null && _savedInstanceState.getBoolean("debug")) {
			arLayout_.modeIs(Mode.DEBUG);
		} else {
			arLayout_.modeIs(Mode.NORMAL);
		}
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[Engine] Created");	
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		enableGPS();
		
		whereaboutsManager_ = new CustomWhereaboutsManager(this);
		
		CustomLocationManager locationManager = whereaboutsManager_.locationManager();
		addressNotifier_ = new AddressNotifier(this, locationManager);
		
		cameraLayout_.setVisibility(View.VISIBLE);
		splashLayout_.setVisibility(View.VISIBLE);
		
		// I'm fully aware that this little trick might cause some inversion between 
		// ready() and onResume() depending on which branch is picked
		// However, this inversion should have no effect
		// TODO: look for a better way...
		if(!initThreadStarted_) {
			Log.v(ConfigurationManager.LOGGING_TAG, "[Engine] Showing splash screen");
			
			// Thread will call ready when done
			initThread_.start();
			initThreadStarted_ = true;
		} else {
			Log.v(ConfigurationManager.LOGGING_TAG, "[Engine] Skipping splash screen");	
			
			Toast.makeText(this, R.string.engine_resumed_from_background, Toast.LENGTH_SHORT).show();
			// Call ready ourselves
			ready();
		}

		Log.v(ConfigurationManager.LOGGING_TAG, "[Engine] Started");	
	}
	
	protected void ready() {
		splashLayout_.setVisibility(View.GONE);
		cameraLayout_.setVisibility(View.VISIBLE);
		arLayout_.setVisibility(View.VISIBLE);
		
		arLayout_.setOnTouchListener(touchListener_);
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[Engine] Ready");	
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(arLayout_ != null) {
			WindowManager w = getWindowManager();
			Display d = w.getDefaultDisplay();
			arLayout_.displayIs(d);
			
			arLayout_.overlayCollectionIs(populate());
		}
		
		if(whereaboutsManager_ != null) {
			arLayout_.connect(whereaboutsManager_);
			ConfigurationManager.instance().deviceCurrentLocationIs(whereaboutsManager_.whereabouts()._1);
			addressNotifier_.connect();
		}
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[Engine] Resumed");	
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if(whereaboutsManager_ != null) {
			arLayout_.disconnect();
			addressNotifier_.disconnect();
		}

		Log.v(ConfigurationManager.LOGGING_TAG, "[Engine] Paused");
	}

	@Override
	public void onStop() {
		super.onStop();
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[Engine] Stopped");	
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(cameraPreview_ != null) {
			cameraPreview_.closeCamera();
		}
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[Engine] Destroyed");	
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
			case ENABLE_GPS_REQUEST:
				enableGPS();
				break;
			default:
				break;
		}
     }


	public synchronized void freezeCamera() {
		// Check if we're within no change period
		long deltaStateChangeTime = System.currentTimeMillis() - lastPauseTime_;
		
		if(deltaStateChangeTime >= MINIMUM_PAUSE_DELAY) {
			lastPauseTime_ = System.currentTimeMillis();
			
			Bitmap picture = cameraPreview_.pauseCamera();

			arLayout_.freeze();
			
			cameraImageView_.setImageBitmap(picture);
			cameraImageView_.setVisibility(View.VISIBLE);
			
			pauseToast_.cancel();
			
			toastImage_.setImageResource(R.drawable.pause);
			pauseToast_.show();

			hapticFeedback();
		}
	}
	
	public synchronized void unfreezeCamera() {// Check if we're within no change period
		long deltaStateChangeTime = System.currentTimeMillis() - lastPauseTime_;
		
		if(deltaStateChangeTime >= MINIMUM_PAUSE_DELAY) {
			lastPauseTime_ = System.currentTimeMillis();
			
			cameraPreview_.unpauseCamera();
			cameraImageView_.setVisibility(View.INVISIBLE);
			
			arLayout_.unfreeze();
			
			pauseToast_.cancel();
			
			toastImage_.setImageResource(R.drawable.play);
			pauseToast_.show();
	
			hapticFeedback();
		}
	}
	
	public synchronized void triggerCamera() {
		if(cameraPreview_.previewState() == PreviewState.FLUID) {
			freezeCamera();
		} else {
			unfreezeCamera();
		}
	}
	
	private final GestureDetector.OnGestureListener gestureListener_ = new GestureDetector.OnGestureListener() {
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			long lastPauseTime;
			
			synchronized(Engine.this) {
				lastPauseTime = lastPauseTime_;
			}
			
			long deltaValidTapTime = System.currentTimeMillis() - lastPauseTime;
			
			if(deltaValidTapTime >= MINIMUM_PAUSE_DELAY) {
				arLayout_.singleClick(e.getX(), e.getY());
			}
			
			return true;
		}
		
		@Override
		public void onShowPress(MotionEvent e) {
			long lastPauseTime;
			
			synchronized(Engine.this) {
				lastPauseTime = lastPauseTime_;
			}
			
			long deltaValidTapTime = System.currentTimeMillis() - lastPauseTime;
			
			if(deltaValidTapTime >= MINIMUM_PAUSE_DELAY) {
				arLayout_.singlePress(e.getX(), e.getY());
			}
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			// ignored
			return false;
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
			long lastPauseTime;
			
			synchronized(Engine.this) {
				lastPauseTime = lastPauseTime_;
			}
			
			long deltaValidTapTime = System.currentTimeMillis() - lastPauseTime;
			
			if(deltaValidTapTime >= MINIMUM_PAUSE_DELAY) {
				arLayout_.longClick(e.getX(), e.getY());
			}
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// ignored
			return false;
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			// ignored
			return false;
		}
		
	};
	
	private final GestureDetector gestureDetector_ = new GestureDetector(gestureListener_);
	
	private final View.OnTouchListener touchListener_ = new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(v == arLayout_) {
				gestureDetector_.onTouchEvent(event);
				
				int pointerCount = event.getPointerCount();
				
				if(pointerCount == 2) {
					triggerCamera();
				}
				
				return true;
			} else {
				return false;
			}
		}
		
	};
	
	private void hapticFeedback() {
		((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(HAPTIC_FEEDBACK_VIBRATION_PATTERN, -1);
	}
	
	private boolean gpsEnabled() {
		LocationManager locationManager = ConfigurationManager.instance().locationManager();
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	private void enableGPS() {
		if(!gpsEnabled()) {
			gpsDialog_.show();
		}
	}

	private OverlayCollection populate() {
		Triple<Location, Azimuth, Inclination> deviceWhereabouts = whereaboutsManager_.whereabouts();
		Location deviceLocation = new Location(deviceWhereabouts._1);
		
		Location l1 = new Location(deviceLocation);
		l1.setLatitude(l1.getLatitude() + 0.0009);
		DummyEntity e1 = new DummyEntity("Jack", "friends", "Jack is a friend!", l1);
		
		Location l2 = new Location(deviceLocation);
		l2.setLatitude(l2.getLatitude() - 0.0004);
		DummyEntity e2 = new DummyEntity("Joe", "friends", "Joe is a friend!", l2);
		
		Location l3 = new Location(deviceLocation);
		l3.setLongitude(l3.getLongitude() + 0.0020);
		DummyEntity e3 = new DummyEntity("John", "friends", "John is a friend!", l3);
		
		Location l4 = new Location(deviceLocation);
		l4.setLongitude(l4.getLongitude() - 0.0012);
		DummyEntity e4 = new DummyEntity("Jimmy", "friends", "Jimmy is a friend!", l4);
		
		List<Overlay> overlayList = new ArrayList<Overlay>(); 
		overlayList.add(new Overlay(this, e1));
		overlayList.add(new Overlay(this, e2));
		overlayList.add(new Overlay(this, e3));
		overlayList.add(new Overlay(this, e4));
		
		OverlayCollection dummyOverlays = new OverlayCollection(overlayList);
		
		OverlayCollection calendarOverlays = new CalendarAdapter(this).overlays();
		
		OverlayCollection mergedOverlays = dummyOverlays.merge(calendarOverlays);
		
		ConfigurationManager.instance().overlayCollectionIs(mergedOverlays);
		
		return mergedOverlays;
	}

}