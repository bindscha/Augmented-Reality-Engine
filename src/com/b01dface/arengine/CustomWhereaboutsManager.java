package com.b01dface.arengine;

import java.util.LinkedList;
import java.util.List;

import com.b01dface.arengine.util.Azimuth;
import com.b01dface.arengine.util.Inclination;
import com.b01dface.arengine.util.Triple;

import android.content.Context;
import android.location.Location;
import android.util.Log;

/**
 * The <code>CustomWhereaboutsManager<code> handles everything related to the device location and orientation.
 * It aggregates location and orientation information and notifies observers.
 * 
 * <BR>
 * 
 * Note: <i>whereabout</i> in this context is used to refer to the combined location and orientation.
 * 
 * @author Laurent Bindschaedler
 */
public class CustomWhereaboutsManager {
	
	public static final long DELAY_SHORT = 10, DELAY_NORMAL = 20, DELAY_LONG = 50;
	
	private final long notificationDelay_;
	
	private final CustomSensorManager customSensorManager_;
	private final CustomLocationManager customLocationManager_;
	
	private Location location_;
	private Azimuth azimuth_;
	private Inclination inclination_;
	
	private boolean hasLocation_;
	private boolean hasSensor_;
	
	private final List<WhereaboutsObserver> whereaboutsObservers_;
	
	private long lastNotification_;
	
	private final SensorObserver sensorObserver_ = new SensorObserver() {
		
		@Override
		public void sensorsChanged(Azimuth _azimuth, Inclination _inclination) {
			Log.v(ConfigurationManager.LOGGING_TAG, "[CustomWhereaboutsManager] Sensor Provider got new sensor values");
			
			// Take snapshot
			List<WhereaboutsObserver> snapshotObservers;
			Location snapshotLocation;
			Azimuth snapshotAzimuth;
			Inclination snapshotInclination;
			long lastNotification;
			
			synchronized(CustomWhereaboutsManager.this) {
				azimuth_ = _azimuth;
				inclination_ = _inclination;
				
				hasSensor_ = true;
				
				// Take snapshot
				snapshotAzimuth = _azimuth;
				snapshotInclination = _inclination;
				snapshotLocation = location_;
				
				snapshotObservers = new LinkedList<WhereaboutsObserver>(whereaboutsObservers_);
				
				lastNotification = lastNotification_;
			}
			
			// Notify with snapshot values
			long deltaNotification = System.currentTimeMillis() - lastNotification;
			if(deltaNotification >= notificationDelay_) {
				Log.d(ConfigurationManager.LOGGING_TAG, "CustomWhereaboutsManager notifying observers.");
				
				synchronized(CustomWhereaboutsManager.this) {
					lastNotification_ = System.currentTimeMillis();
				}
				
				notifyObservers(snapshotObservers, snapshotLocation, snapshotAzimuth, snapshotInclination);
			}
		}
		
	};
	
	private final LocationObserver locationObserver_ = new LocationObserver() {
		
		@Override
		public void locationChanged(Location _location) {
			Log.v(ConfigurationManager.LOGGING_TAG, "[CustomWhereaboutsManager] GPS Provider got new location");
			
			// Take snapshot
			List<WhereaboutsObserver> snapshotObservers;
			Location snapshotLocation;
			Azimuth snapshotAzimuth;
			Inclination snapshotInclination;
			long lastNotification;
			
			synchronized(CustomWhereaboutsManager.this) {
				location_ = new Location(_location);
				
				hasLocation_ = true;
				
				// Take snapshot
				snapshotAzimuth = azimuth_;
				snapshotInclination = inclination_;
				snapshotLocation = new Location(_location);
				
				snapshotObservers = new LinkedList<WhereaboutsObserver>(whereaboutsObservers_);
				
				lastNotification = lastNotification_;
			}
			
			// Notify with snapshot values
			long deltaNotification = System.currentTimeMillis() - lastNotification;
			if(deltaNotification >= notificationDelay_) {			
				synchronized(CustomWhereaboutsManager.this) {
					lastNotification_ = System.currentTimeMillis();
				}
				
				notifyObservers(snapshotObservers, snapshotLocation, snapshotAzimuth, snapshotInclination);
			}
		}
		
	};
	
	public CustomWhereaboutsManager(Context _context) {
		this(_context, DELAY_NORMAL);
	}
	
	public CustomWhereaboutsManager(Context _context, long _notificationDelay) {
		location_ = new Location(ConfigurationManager.DEFAULT_LOCATION);
		azimuth_ = ConfigurationManager.DEFAULT_AZIMUTH;
		inclination_ = ConfigurationManager.DEFAULT_INCLINATION;
		
		hasLocation_ = false;
		hasSensor_ = false;
		
		notificationDelay_ = _notificationDelay;
		lastNotification_ = 0;
		
		whereaboutsObservers_ = new LinkedList<WhereaboutsObserver>();
		
		customLocationManager_ = new CustomLocationManager(_context);
		customSensorManager_ = new CustomSensorManager(_context);
		
		//location_ = customLocationManager_.currentLocation();
		location_ = new Location("custom");
		location_.setLatitude(46.518873);
		location_.setLongitude(6.561981);
		location_.setAltitude(450);
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[CustomWhereaboutsManager] Created");
	}
	
	public synchronized void connect() {
		customLocationManager_.locationObserverAdd(locationObserver_);
		customSensorManager_.sensorObserverAdd(sensorObserver_);
		
		customLocationManager_.connect();
		customSensorManager_.connect();
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[CustomWhereaboutsManager] Connected");
	}
	
	public synchronized void disconnect() {
		customLocationManager_.locationObserverRem(locationObserver_);
		customSensorManager_.sensorObserverRem(sensorObserver_);
		
		customLocationManager_.disconnect();
		customSensorManager_.disconnect();
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[CustomWhereaboutsManager] Disconnected");
	}

	public synchronized CustomLocationManager locationManager() {
		return customLocationManager_;
	}
	
	public synchronized CustomSensorManager sensorManager() {
		return customSensorManager_;
	}
	
	public synchronized Triple<Location, Azimuth, Inclination> whereabouts() {
		return new Triple<Location, Azimuth, Inclination>(new Location(location_), azimuth_, inclination_);
	}

	public synchronized boolean validValues() {
		return hasLocation_ && hasSensor_;
	}
	
	public synchronized void whereaboutsObserverAdd(WhereaboutsObserver _whereaboutsObserver) {
		if (!whereaboutsObservers_.contains(_whereaboutsObserver)) {
			whereaboutsObservers_.add(_whereaboutsObserver);
		}
	}

	public synchronized void whereaboutsObserverRem(WhereaboutsObserver _whereaboutsObserver) {
		if (whereaboutsObservers_.contains(_whereaboutsObserver)) {
			whereaboutsObservers_.remove(_whereaboutsObserver);
		}
	}

	// Notify with snapshot values to avoid holding the monitor's lock
	private void notifyObservers(List<WhereaboutsObserver> _whereaboutsObservers, Location _location, Azimuth _azimuth, Inclination _inInclination) {
		for(WhereaboutsObserver whereaboutsObserver : _whereaboutsObservers) {
			whereaboutsObserver.whereaboutsChanged(new Location(_location), _azimuth, _inInclination);
		}
	}

}
