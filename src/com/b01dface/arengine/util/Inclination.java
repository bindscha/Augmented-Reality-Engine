package com.b01dface.arengine.util;

import java.text.DecimalFormat;

/**
 * <code>Inclination</code> represents an inclination angle.
 * 
 * @author Laurent Bindschaedler
 */
// KNOWN ISSUES
//
// If relative angles are about 180 or -180, we could have a problem
// with the values oscillating between negative and positive on slight tilts
public class Inclination {

	public static class RelativeInclination {
		
		private static final int RANGE = INCLINATION_RANGE * 2;
		
		private final double value_;
		
		public RelativeInclination(double _value) {
			if(_value == RANGE) {
				value_ = RANGE;
			} else if(_value == -RANGE) {
				value_ = -RANGE;
			} else {
				value_ = _value % RANGE;
			}
			
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
		
		public double absoluteDoubleValue() {
			if(value_ < 0.0) {
				return -value_;
			} else {
				return value_;
			}
		}
		
		public float absoluteFloatValue() {
			return (float) absoluteDoubleValue();
		}
		
		public int absoluteIntValue() {
			return (int) absoluteDoubleValue();
		}
		
		@Override
		public String toString() {
			return String.valueOf(DEGREE_FORMAT.format(value_));
		}
		
		private void valid() {
			if(value_ < -RANGE || value_ > RANGE) {
				throw new RuntimeException("Relative azimuth must be comprised between " + (-RANGE) + " and " + RANGE + " degrees!");
			}
		}
		
	}
	
	private static final int INCLINATION_RANGE = 90;
	private static final DecimalFormat DEGREE_FORMAT = new DecimalFormat("#.##");
	
	private final double value_;
	
	public Inclination(double _value) {
		if(_value == INCLINATION_RANGE) {
			value_ = INCLINATION_RANGE;
		} else if(_value == -INCLINATION_RANGE) {
			value_ = -INCLINATION_RANGE;
		} else {
			value_ = _value % INCLINATION_RANGE;
		}
		
		valid();
	}
	
	public double doubleValue() {
		return value_;
	}
	
	public float floatValue() {
		return (float) value_;
	}
	
	public int intValue() {
		return (int) value_;
	}
	
	public RelativeInclination inclinationTo(Inclination _referenceInclination) {
		double deltaInclination = value_ - _referenceInclination.value_;
		
		return new RelativeInclination(deltaInclination);
	}
	
	@Override
	public String toString() {
		return String.valueOf(DEGREE_FORMAT.format(value_));
	}
	
	private void valid() {
		if(value_ < -INCLINATION_RANGE || value_ > INCLINATION_RANGE) {
			throw new RuntimeException("Inclination must be comprised between " + (-INCLINATION_RANGE) + " and " + INCLINATION_RANGE + " degrees!");
		}
	}
	
}
