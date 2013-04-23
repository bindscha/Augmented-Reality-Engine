package com.b01dface.arengine;

import java.util.LinkedList;
import java.util.List;

import com.b01dface.arengine.util.Azimuth;
import com.b01dface.arengine.util.Inclination;
import com.b01dface.arengine.util.MathTools;
import com.b01dface.arengine.util.Pair;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * The <code>CustomSensorManager<code> handles everything related to the device orientation.
 * It samples the values from each sensor as necessary, filters and interpolates the values smoothly
 * and handles the notification of sensor observers.
 * 
 * @author Laurent Bindschaedler
 */
public class CustomSensorManager {

	private final SensorManager sensorManager_;
	
	private volatile double azimuth_;
	private volatile double inclination_;
	private volatile double pitch_;
	
	private final double kFilteringFactorAzimuth_ = 0.2;
	private final double kFilteringFactorInclination_ = 0.1;
	
	private final List<SensorObserver> sensorObservers_;
	
	private boolean hasAzimuth_, hasInclination_, hasPitch_;
	
	private final SensorEventListener listener_ = new SensorEventListener() {
		
		@Override
		public void onAccuracyChanged(Sensor _sensor, int _accuracy) {
			// Do nothing
		}

		@Override
		public void onSensorChanged(SensorEvent _event) {
			Log.v(ConfigurationManager.LOGGING_TAG, "[CustomSensorManager] Sensor Provider has new sensor values");
			
			// Take snapshot
			List<SensorObserver> snapshotObservers;
			double snapshotAzimuth, snapshotPitch, snapshotInclination;
			
			float sensorEventValues[] = _event.values;
			
			if (_event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				// On orientation changed, update azimuth and pitch
				
				double azimuth = sensorEventValues[0];
				double pitch = sensorEventValues[1];
				
				// Update angles accordingly
				synchronized(CustomSensorManager.this) {
					// Compute filtered azimuth using k-filtering
					// Essentially, this is k * new + (1 - k) * old
					// In practice, we need to watch for wraparound (e.g. new = 1, old = 359)
					double deltaAzimuth1 = azimuth - azimuth_;
					double deltaAzimuth2 = 0.0;
					
					if(deltaAzimuth1 < 0.0) {
						deltaAzimuth2 = deltaAzimuth1 + 360.0;
					} else {
						deltaAzimuth2 = deltaAzimuth1 - 360.0;
					}
					
					double delta = MathTools.minAbs(deltaAzimuth1, deltaAzimuth2);
					
					double filteredAzimuth = MathTools.mod(kFilteringFactorAzimuth_ * (azimuth_ + delta) + (1 - kFilteringFactorAzimuth_) * azimuth_, 360);
					azimuth_ = filteredAzimuth;
					pitch_ = pitch;
					
					hasAzimuth_ = true;
					hasPitch_ = true;
					
					// Take snapshot
					snapshotAzimuth = azimuth_;
					snapshotPitch = pitch_;
					snapshotInclination = inclination_;
					
					snapshotObservers = new LinkedList<SensorObserver>(sensorObservers_);
				}
				
			} else if (_event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				// On accelerometer changed, update inclination
				
				double inclination = 0.0;
				
				double rollingZ = sensorEventValues[2];
				double rollingX = sensorEventValues[0];

				// Compute device inclination
				if (rollingZ != 0.0) {
					inclination = Math.atan(rollingX / rollingZ);
				} else {
					inclination = Math.PI / 2.0;
				}

				// Convert inclination to degrees
				inclination = inclination * (360 / (2 * Math.PI));
				
				// Flip inclination accordingly
				if(rollingX >= 0.0) {
					if (inclination < 0) {
						inclination = inclination + 90;
					} else {
						inclination = inclination - 90;
					}
				} else {
					if(inclination < 0) {
						inclination = -inclination - 90;
					} else {
						inclination = -inclination + 90;
					}
				}
				
				synchronized(CustomSensorManager.this) {
					// Filter inclination according to k-filtering algorithm
					double filteredInclination = kFilteringFactorInclination_ * inclination + (1.0 - kFilteringFactorInclination_) * inclination_;
					
					inclination_ = filteredInclination;
					
					hasInclination_ = true;
					
					// Take snapshot
					snapshotAzimuth = azimuth_;
					snapshotPitch = pitch_;
					snapshotInclination = inclination_;
					
					snapshotObservers = new LinkedList<SensorObserver>(sensorObservers_);
				}
				
			} else {
				// Do nothing and return right away (to avoid notifying observers when nothing relevant happened)
				// This is more of a safety
				return;
			}
			
			// Notify with snapshot values
			notifyObservers(snapshotObservers, snapshotAzimuth, snapshotInclination);
		}
		
	};
	
	public CustomSensorManager(Context _context) {
		azimuth_ = ConfigurationManager.DEFAULT_AZIMUTH.doubleValue();
		inclination_ = ConfigurationManager.DEFAULT_INCLINATION.doubleValue();
		pitch_ = ConfigurationManager.DEFAULT_PITCH;
		
		hasAzimuth_ = false;
		hasInclination_ = false;
		hasPitch_ = false;
		
		sensorObservers_ = new LinkedList<SensorObserver>();
		
		sensorManager_ = (SensorManager) _context.getSystemService(Context.SENSOR_SERVICE);
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[CustomSensorManager] Created");
	}
	
	public synchronized void connect() {
		sensorManager_.registerListener(listener_, sensorManager_.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
		sensorManager_.registerListener(listener_, sensorManager_.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
	
		Log.v(ConfigurationManager.LOGGING_TAG, "[CustomSensorManager] Connected");
	}
	
	public synchronized void disconnect() {
		sensorManager_.unregisterListener(listener_);
		Log.v(ConfigurationManager.LOGGING_TAG, "[CustomSensorManager] Disconnected");
	}

	public synchronized Pair<Azimuth, Inclination> sensor() {
		return new Pair<Azimuth, Inclination>(new Azimuth(azimuth_), new Inclination(inclination_));
	}
	
	public synchronized boolean validValues() {
		return hasAzimuth_ && hasInclination_ && hasPitch_;
	}

	public synchronized void sensorObserverAdd(SensorObserver _sensorObserver) {
		if (!sensorObservers_.contains(_sensorObserver)) {
			sensorObservers_.add(_sensorObserver);
		}
	}

	public synchronized void sensorObserverRem(SensorObserver _sensorObserver) {
		if (sensorObservers_.contains(_sensorObserver)) {
			sensorObservers_.remove(_sensorObserver);
		}
	}
	
	// Notify with snapshot values to avoid holding the monitor's lock
	private void notifyObservers(List<SensorObserver> _sensorObservers, double _azimuth, double _inclination) {
		for(SensorObserver sensorObserver : _sensorObservers) {
			sensorObserver.sensorsChanged(new Azimuth(_azimuth), new Inclination(_inclination));
		}
	}

}
