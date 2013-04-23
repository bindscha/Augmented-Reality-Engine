package com.b01dface.arengine.overlays;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import android.location.Location;

import com.b01dface.arengine.ConfigurationManager;
import com.b01dface.arengine.DeviceDatabase;
import com.b01dface.arengine.DeviceDatabase.DeviceInformation;
import com.b01dface.arengine.util.Azimuth;
import com.b01dface.arengine.util.Distance;
import com.b01dface.arengine.util.Inclination;
import com.b01dface.arengine.util.Inclination.RelativeInclination;
import com.b01dface.arengine.util.Triple;
import com.b01dface.arengine.util.Azimuth.RelativeAzimuth;

/**
 * <code>OverlayCollection</code> is a class to aggregate overlays.
 * 
 * @author Laurent Bindschaedler
 */
public class OverlayCollection {

	private final Map<UUID, Overlay> overlays_;
	private final double horizontalAngle_, verticalAngle_;
	
	public OverlayCollection() {		
		DeviceInformation deviceInformation = DeviceDatabase.instance().deviceInformation();
		double safetyMargin = ConfigurationManager.ANGLE_SAFETY_MARGIN;
		horizontalAngle_ = (deviceInformation.horizontalViewAngle() * (1.0 + safetyMargin)) / 2.0;
		verticalAngle_ = (deviceInformation.verticalViewAngle() * (1.0 + safetyMargin)) / 2.0;
		
		overlays_ = new HashMap<UUID, Overlay>();
	}
	
	public OverlayCollection(Collection<Overlay> _overlays) {
		this();
		
		for(Overlay overlay : _overlays) {
			overlays_.put(overlay.id(), overlay);
		}
	}
	
	public OverlayCollection merge(OverlayCollection _otherCollection) {
		if(_otherCollection == null || _otherCollection.overlays_.isEmpty()) {
			return this;
		} else {
			HashSet<Overlay> resultSet = new HashSet<Overlay>(overlays_.values());
			resultSet.addAll(_otherCollection.overlays_.values());

			return new OverlayCollection(resultSet);
		}
	}
	
	public Overlay overlay(UUID _id) {
		return overlays_.get(_id);
	}
	
	public Iterable<Overlay> overlays() {
		return overlays_.values();
	}
	
	public Iterable<Overlay> overlays(Location _location, Azimuth _azimuth, Inclination _inclination) {
		HashSet<Overlay> overlays = new HashSet<Overlay>();
		
		for(Overlay overlay : overlays_.values()) {
			Triple<Distance, Azimuth, Inclination> overlayValues = overlay.values();
			RelativeAzimuth relativeAzimuth = _azimuth.azimuthTo(overlayValues._2);
			RelativeInclination relativeInclination = _inclination.inclinationTo(overlayValues._3);
			
			if(relativeAzimuth.absoluteDoubleValue() <= horizontalAngle_ && relativeInclination.absoluteDoubleValue() <= verticalAngle_) {
				overlays.add(overlay);
			}
		}
		
		return overlays;
	}
	
	public void updateOverlays(Location _deviceLocation) {
		for(Overlay overlay : overlays_.values()) {
			overlay.updateValues(_deviceLocation);
		}
	}
	
}
