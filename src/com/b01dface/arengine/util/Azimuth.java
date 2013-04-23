package com.b01dface.arengine.util;

import java.text.DecimalFormat;

/**
 * <code>Azimuth</code> represents an azimuth angle.
 * 
 * @author Laurent Bindschaedler
 */
// KNOWN ISSUES
//
// If relative angles are about 180 or -180, we could have a problem
// with the values oscillating between negative and positive on slight tilts
public class Azimuth {

	public static class RelativeAzimuth {
		
		private static final int RANGE = AZIMUTH_RANGE * 2;
		
		private final double value_;
		
		public RelativeAzimuth(double _value) {
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
	
	private static final int AZIMUTH_RANGE = 180;
	private static final DecimalFormat DEGREE_FORMAT = new DecimalFormat("#.##");
	
	private final double value_;
	
	public Azimuth(double _value) {
		double rangedValue = MathTools.mod(_value, 2 * AZIMUTH_RANGE);
		
		if(rangedValue >= 0 && rangedValue < AZIMUTH_RANGE) {
			value_ = rangedValue;
		} else if(rangedValue >= AZIMUTH_RANGE && rangedValue < 2 * AZIMUTH_RANGE) {
			value_ = rangedValue - 2 * AZIMUTH_RANGE;
		} else {
			throw new RuntimeException("Unexpected rangedValue in Azimuth.java!");
		}
		
		valid();
	}
	
	public double doubleValue() {
		if(value_ >= 0 && value_ < AZIMUTH_RANGE) {
			return value_;
		} else {
			return value_ + 2 * AZIMUTH_RANGE;
		}
	}
	
	public float floatValue() {
		return (float) doubleValue();
	}
	
	public int intValue() {
		return (int) doubleValue();
	}
	
	public RelativeAzimuth azimuthTo(Azimuth _referenceAzimuth) {
		double deltaAzimuth1 = value_ - _referenceAzimuth.value_;
		double deltaAzimuth2 = 0.0;
		
		if(deltaAzimuth1 < 0.0) {
			deltaAzimuth2 = deltaAzimuth1 + 360.0;
		} else {
			deltaAzimuth2 = deltaAzimuth1 - 360.0;
		}
		
		// Relative azimuth is the one angle out of the two which is smallest in absolute value
		return new RelativeAzimuth(MathTools.minAbs(deltaAzimuth1, deltaAzimuth2));
	}
	
	@Override
	public String toString() {
		return String.valueOf(DEGREE_FORMAT.format(doubleValue()));
	}
	
	private void valid() {
		if(value_ < -AZIMUTH_RANGE || value_ > AZIMUTH_RANGE) {
			throw new RuntimeException("Azimuth must be comprised between " + (-AZIMUTH_RANGE) + " and " + AZIMUTH_RANGE + " degrees!");
		}
	}
	
}
