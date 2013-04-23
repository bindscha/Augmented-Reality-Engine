package com.b01dface.arengine;

import android.location.Location;

/**
 * <code>LocationObserver</code> is the interface used for classes to obtain location information from
 * the <code>CustomLocationObserver</code>.
 * 
 * @author Laurent Bindschaedler
 */
public interface LocationObserver {
	
	public void locationChanged(Location _location);

}
