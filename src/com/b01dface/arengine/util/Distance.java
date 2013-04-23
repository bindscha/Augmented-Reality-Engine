package com.b01dface.arengine.util;

import java.text.DecimalFormat;

/**
 * <code>Distance</code> represents a distance in meters.
 * 
 * @author Laurent Bindschaedler
 */
public class Distance {

	private static final DecimalFormat DISTANCE_FORMAT = new DecimalFormat("#.##");
	
	private final double value_;
	
	public Distance(double _distance) {
		value_ = _distance;
		
		valid();
	}
	
	public double doubleValue() {
		return value_;
	}
	
	public float floatValue() {
		return (float) doubleValue();
	}
	
	public int intValue() {
		return (int) doubleValue();
	}
	
	@Override
	public String toString() {
		return String.valueOf(DISTANCE_FORMAT.format(doubleValue()));
	}
	
	private void valid() {
		if(value_ < 0) {
			throw new RuntimeException("Distance cannot be negative!");
		}
	}

}
