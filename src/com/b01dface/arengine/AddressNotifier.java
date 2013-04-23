package com.b01dface.arengine;

import java.io.IOException;
import java.util.List;

import com.b01dface.arlo.core.R;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.widget.Toast;

/**
 * The <code>AddressNotifier</code> is tasked with geocoding.
 * 
 * @author Laurent Bindschaedler
 */
public class AddressNotifier {

	private class NotificationThread extends Thread {
		
		private static final int MAX_RESULTS = 1;
		
		private boolean alive_ = true;
		
		private Geocoder geocoder_ = new Geocoder(context_);
		
		@Override
		public void run() {
			synchronized(AddressNotifier.this) {
				while(alive_) {
					try {
						AddressNotifier.this.wait();
					} catch (InterruptedException e1) {
						// ignored
					}
					
					try {
						List<Address> addresses = geocoder_.getFromLocation(currentLocation_.getLatitude(), currentLocation_.getLongitude(), MAX_RESULTS);
						if(addresses != null && addresses.size() == 1) {
							Address address = addresses.get(0);
							StringBuilder addressText = new StringBuilder();
							for(int i = 0; i < address.getMaxAddressLineIndex(); ++i) {
								addressText.append(address.getAddressLine(i) + "\n");
							}
							String country = address.getCountryName();
							if(country != null) {
								addressText.append(country);
							}
							
							String toastMessage = context_.getString(R.string.new_location) + ":" + "\n" + addressText.toString();
							
							Toast.makeText(context_, toastMessage, Toast.LENGTH_LONG).show();
						}
					} catch (IOException e) {
						// ignored
					}
				}
			}
		}
		
		public void kill() {
			alive_ = false;
		}
		
	}
	
	public static final double MINIMUM_NOTIFICATION_DISTANCE = 10;

	private final Context context_;
	private final CustomLocationManager locationManager_;
	
	private Location currentLocation_;
	
	private NotificationThread notificationThread_;
	
	private final LocationObserver locationObserver_ = new LocationObserver() {
		
		@Override
		public void locationChanged(Location _location) {
			ConfigurationManager.instance().deviceCurrentLocationIs(_location);
			
			synchronized(this) {
				if(_location.distanceTo(currentLocation_) > MINIMUM_NOTIFICATION_DISTANCE)
				
				currentLocation_ = new Location(_location);
				
				notify();
			}
		}
	};
	
	public AddressNotifier(Context _context, CustomLocationManager _locationManager) {
		context_ = _context;
		locationManager_ = _locationManager;
		
		currentLocation_ = null;
		
		notificationThread_ = null;
	}
	
	public synchronized void connect() {
		if(notificationThread_ == null) {
			notificationThread_ = new NotificationThread();
			notificationThread_.start();
			
			locationManager_.locationObserverAdd(locationObserver_);
		}
	}
	
	public synchronized void disconnect() {
		if(notificationThread_ != null) {
			notificationThread_.kill();
			notificationThread_ = null;
			
			locationManager_.locationObserverRem(locationObserver_);
		}
	}
	
}
