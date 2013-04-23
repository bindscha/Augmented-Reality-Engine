package com.b01dface.arengine;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.b01dface.arengine.overlays.OverlayCollection;
import com.b01dface.arengine.util.Azimuth;
import com.b01dface.arengine.util.Distance;
import com.b01dface.arengine.util.Inclination;
import com.b01dface.arlo.core.R;
import android.content.Context;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

public class ConfigurationManager {

	public static final String LOGGING_TAG = "ARE";
	
	public static final Location DEFAULT_LOCATION = new Location("custom");
	public static final Distance DEFAULT_DISTANCE = new Distance(100);
	public static final Azimuth DEFAULT_AZIMUTH = new Azimuth(0.0);
	public static final double DEFAULT_PITCH = 0.0;
	public static final Inclination DEFAULT_INCLINATION = new Inclination(0.0);
	
	public static final double ANGLE_SAFETY_MARGIN = 0.1;
	
	public static final String DEFAULT_PROVIDER = "unknown";
	
	private static Context context_;
	
	private OverlayCollection overlayCollection_;
	
	private Location deviceCurrentLocation_;
	
	private static class ConfigurationManagerInner {
		
		public static final ConfigurationManager CONFIGURATION_MANAGER = new ConfigurationManager();
		
	}
	
	private ConfigurationManager() {
	}
	
	public static Context context() {
		return context_;
	}
	
	public static void contextIs(Context _context) {
		context_ = _context;
	}
	
	public static ConfigurationManager instance() {
		return ConfigurationManagerInner.CONFIGURATION_MANAGER;
	}
	
	public void overlayCollectionIs(OverlayCollection _overlayCollection) {
		overlayCollection_ = _overlayCollection;
	}
	
	public OverlayCollection overlayCollection() {
		return overlayCollection_;
	}
	
	public void deviceCurrentLocationIs(Location _deviceCurrentLocation) {
		deviceCurrentLocation_ = _deviceCurrentLocation;
	}
	
	public Location deviceCurrentLocation() {
		return deviceCurrentLocation_;
	}
	
	public SensorManager sensorManager() {
		valid();
		
		return (SensorManager) context_.getSystemService(Context.SENSOR_SERVICE);
	}
	
	public LocationManager locationManager() {
		valid();
		
		return (LocationManager) context_.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public Location locationFromString(String _location, String _provider) {
		Geocoder geocoder = new Geocoder(context_);
		
		if(_location == null || _location.trim().length() == 0) {
			return null;
		}
		
		try {
			List<Address> addresses = geocoder.getFromLocationName(_location, 1);
			if(addresses != null && addresses.size() == 1) {
				Address address = addresses.get(0);
				
				if(_provider == null) {
					_provider = DEFAULT_PROVIDER;
				}
				
				if(address.hasLatitude() && address.hasLongitude()) {
					Location location = new Location(_provider);
					location.setLatitude(address.getLatitude());
					location.setLongitude(address.getLongitude());
					
					return location;
				}
			}
			
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	
	public String calendarProvider() {
		if(Build.VERSION.RELEASE.startsWith("1.0") || Build.VERSION.RELEASE.contains("2.0")) {
			return "calendar";
		} else {
			return "com.android.calendar";
		}
	}
	
	public InputStream deviceDatabaseInputStream() {
		valid();
		
		return context_.getResources().openRawResource(R.raw.device_database);
	}
	
	public double scaleFactor() {
		return context_.getResources().getDisplayMetrics().density;
	}
	
	private void valid() {
		if(context_ == null) {
			throw new IllegalStateException("Context reference cannot be null!");
		}
	}
	
}
