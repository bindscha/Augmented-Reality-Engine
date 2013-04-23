package com.b01dface.arengine.util;

/**
 * Some mathematics tools.
 * 
 * @author Laurent Bindschaedler
 */
public class MathTools {

	public static double mod(double _a, int _m) {
		double remainder = _a % _m;
		if(remainder < 0.0) {
			return remainder + _m;
		} else {
			return remainder;
		}
	}
	
	public static double min(double _a, double _b) {
		if(_a < _b) {
			return _a;
		} else {
			return _b;
		}
	}
	
	public static double abs(double _a) {
		if(_a < 0.0) {
			return -_a;
		} else {
			return _a;
		}
	}
	
	public static double minAbs(double _a, double _b) {
		double aAbs = abs(_a);
		double bAbs = abs(_b);
		
		if(aAbs < bAbs) {
			return _a;
		} else {
			return _b;
		}
	}
	
}
