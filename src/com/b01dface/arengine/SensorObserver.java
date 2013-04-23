package com.b01dface.arengine;

import com.b01dface.arengine.util.Azimuth;
import com.b01dface.arengine.util.Inclination;

/**
 * <code>SensorObserver</code> is the interface used for classes to obtain sensor information from
 * the <code>CustomSensorObserver</code>.
 * 
 * @author Laurent Bindschaedler
 */
public interface SensorObserver {

	public void sensorsChanged(Azimuth _azimuth, Inclination _inclination);
	
}
