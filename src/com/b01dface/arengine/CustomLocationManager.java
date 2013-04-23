package com.b01dface.arengine;

import java.util.LinkedList;
import java.util.List;

import com.b01dface.arlo.core.R;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * The <code>CustomLocationManager<code> handles everything related to the device location.
 * It samples the location as necessary and handles the notification of location observers.
 * 
 * @author Laurent Bindschaedler
 */
public class CustomLocationManager {
	
	public static final long MINIMUM_UPDATE_TIME = 1000;
	public static final int MINIMUM_UPDATE_DISTANCE = 1;
	
	private final Context context_;
	
	private final LocationManager locationManager_;

	private volatile Location location_;
	
	private final List<LocationObserver> locationObservers_;
	
	private boolean hasLocation_;
	
	private final LocationListener listener_ = new LocationListener() {
		
		@Override
		public void onStatusChanged(String _provider, int _status, Bundle _extras) {
			switch (_status) {
			case LocationProvider.OUT_OF_SERVICE:
				Log.v(ConfigurationManager.LOGGING_TAG, "[CustomLocationManager] GPS Status Changed: Out of Service");
				Toast.makeText(context_, R.string.gps_status_out_of_service, Toast.LENGTH_SHORT).show();
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				Log.v(ConfigurationManager.LOGGING_TAG, "[CustomLocationManager] GPS Status Changed: Temporarily Unavailable");
				Toast.makeText(context_, R.string.gps_status_temporarily_unavailable, Toast.LENGTH_SHORT).show();
				break;
			case LocationProvider.AVAILABLE:
				Log.v(ConfigurationManager.LOGGING_TAG, "[CustomLocationManager] GPS Status Changed: Available");
				Toast.makeText(context_, R.string.gps_status_available, Toast.LENGTH_SHORT).show();
				break;
			}

		}
		
		@Override
		public void onProviderEnabled(String _provider) {
			Log.v(ConfigurationManager.LOGGING_TAG, "[CustomLocationManager] GPS Provider Enabled");
		}
		
		@Override
		public void onProviderDisabled(String _provider) {
			Log.v(ConfigurationManager.LOGGING_TAG, "[CustomLocationManager] GPS Provider Disabled");
		}
		
		@Override
		public void onLocationChanged(Location _location) {
			Log.v(ConfigurationManager.LOGGING_TAG, "[CustomLocationManager] GPS Provider has new location");
			
			// Take snapshot
			List<LocationObserver> snapshotObservers;
			Location snapshotLocation;
			
			synchronized(CustomLocationManager.this) {
				location_ = new Location(_location);
				
				hasLocation_ = true;
				
				// Take snapshot
				snapshotLocation = new Location(_location);

				snapshotObservers = new LinkedList<LocationObserver>(locationObservers_);
			}
			
			// Notify with snapshot values
			notifyObservers(snapshotObservers, snapshotLocation);
		}
	};

	public CustomLocationManager(Context _context) {
		location_ = new Location(ConfigurationManager.DEFAULT_LOCATION);
		
		hasLocation_ = false;
		
		locationObservers_ = new LinkedList<LocationObserver>();
		
		context_ = _context;
		
		locationManager_ = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);
		
		Location lastKnownLocation = locationManager_.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		if(lastKnownLocation != null) {
			location_ = lastKnownLocation;
		}
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[CustomLocationManager] Created");
	}
	
	public synchronized void connect() {
		locationManager_.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_UPDATE_TIME, MINIMUM_UPDATE_DISTANCE, listener_);
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[CustomLocationManager] Connected");
	}
	
	public synchronized void disconnect() {
		locationManager_.removeUpdates(listener_);
		Log.v(ConfigurationManager.LOGGING_TAG, "[CustomLocationManager] Disconnected");
	}

	public synchronized Location currentLocation() {
		return new Location(location_);
	}
	
	public synchronized boolean validValues() {
		return hasLocation_;
	}

	public synchronized void locationObserverAdd(LocationObserver _locationObserver) {
		if (!locationObservers_.contains(_locationObserver)) {
			locationObservers_.add(_locationObserver);
		}
	}

	public synchronized void locationObserverRem(LocationObserver _locationObserver) {
		if (locationObservers_.contains(_locationObserver)) {
			locationObservers_.remove(_locationObserver);
		}
	}

	// Notify with snapshot values to avoid holding the monitor's lock
	private void notifyObservers(List<LocationObserver> _locationObservers, Location _location) {
		for (LocationObserver locationObserver : _locationObservers) {
			locationObserver.locationChanged(new Location(_location));
		}
	}
	
}
