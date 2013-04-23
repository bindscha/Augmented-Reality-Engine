package com.b01dface.arengine;

import android.location.Location;

import com.b01dface.arengine.util.Azimuth;
import com.b01dface.arengine.util.Inclination;

/**
 * <code>WhereaboutsObserver</code> is the interface used for classes to obtain whereabouts information from
 * the <code>CustomWhereaboutsObserver</code>.
 * 
 * @author Laurent Bindschaedler
 */
public interface WhereaboutsObserver {

	public void whereaboutsChanged(Location _location, Azimuth _azimuth, Inclination _inclination);
	
}
