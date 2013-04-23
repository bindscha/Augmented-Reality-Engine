package com.b01dface.arengine;

import com.b01dface.arengine.DeviceDatabase.DeviceInformation;
import com.b01dface.arengine.overlays.Overlay;
import com.b01dface.arengine.overlays.OverlayCollection;
import com.b01dface.arengine.util.Azimuth;
import com.b01dface.arengine.util.Distance;
import com.b01dface.arengine.util.Inclination;
import com.b01dface.arengine.util.Inclination.RelativeInclination;
import com.b01dface.arengine.util.Triple;
import com.b01dface.arengine.util.Azimuth.RelativeAzimuth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;

/**
 * The <code>ARLayout</code> is part of the display subsystem which is tasked with handling
 * the layout and drawing of the various overlays to be displayed on the screen.
 * 
 * @author Laurent Bindschaedler
 */
public class ARLayout extends View implements WhereaboutsObserver {
	
	public static enum Mode {
		DEBUG, NORMAL
	}
	
	public static enum PreviewState {
		FLUID, FROZEN;
	}

	public static final int MINIMUM_CAMERA_TRIGGER_DELAY = 50;

	public static final double TOUCH_ERROR_MARGIN = 0.1;
	
	public static int DEFAULT_SCREEN_WIDTH = 480;
	public static int DEFAULT_SCREEN_HEIGHT = 320;
	
	private double screenWidth_ = DEFAULT_SCREEN_WIDTH;
	private double screenHeight_ = DEFAULT_SCREEN_HEIGHT;

	private final double xAngleWidth_;
	private final double yAngleWidth_;

	private OverlayCollection overlays_;

	private volatile Location deviceLocation_;
	private volatile Azimuth deviceAzimuth_;
	private volatile Inclination deviceInclination_;

	private Mode mode_ = Mode.NORMAL;
	
	private PreviewState previewState_;
	
	private CustomWhereaboutsManager whereaboutsManager_;
	
	private boolean hasHighlighted_;

	public ARLayout(Context _context) {
		this(_context, null);
	}
	
	public ARLayout(Context _context, AttributeSet _attributeSet) {
		this(_context, _attributeSet, 0);
	}
	
	public ARLayout(Context _context, AttributeSet _attributeSet, int _defaultStyle) {
		super(_context, _attributeSet, _defaultStyle);
		
		overlays_ = new OverlayCollection();
		
		deviceLocation_ = new Location(ConfigurationManager.DEFAULT_LOCATION);
		deviceAzimuth_ = ConfigurationManager.DEFAULT_AZIMUTH;
		deviceInclination_ = ConfigurationManager.DEFAULT_INCLINATION;
		
		DeviceInformation deviceInformation = DeviceDatabase.instance().deviceInformation();
		
		if(deviceInformation != null) {
			xAngleWidth_ = deviceInformation.horizontalViewAngle() / 2.0;
			yAngleWidth_ = deviceInformation.verticalViewAngle() / 2.0;
		} else {
			xAngleWidth_ = DeviceDatabase.DEFAULT_HORIZONTAL_VIEW_ANGLE;
			yAngleWidth_ = DeviceDatabase.DEFAULT_VERTICAL_VIEW_ANGLE;
		}
		
		previewState_ = PreviewState.FLUID;

		hasHighlighted_ = false;
		
		valid();
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[ARLayout] Created");	
	}
	
	public synchronized void displayIs(Display _display) {
		screenWidth_ = _display.getWidth();
		screenHeight_ = _display.getHeight();
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[ARLayout] Display dimensions are " + screenWidth_ + "x" + screenHeight_);
	}
	
	public synchronized void overlayCollectionIs(OverlayCollection _overlays) {
		overlays_ = _overlays;
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[ARLayout] Received overlay collection");	
	}

	public synchronized void connect(CustomWhereaboutsManager _whereaboutsManager) {
		whereaboutsManager_ = _whereaboutsManager;
		
		if(whereaboutsManager_.validValues()) {
			Triple<Location, Azimuth, Inclination> whereabouts = whereaboutsManager_.whereabouts();
			deviceLocation_ = whereabouts._1;
			deviceAzimuth_ = whereabouts._2;
			deviceInclination_ = whereabouts._3;
		}
		
		whereaboutsManager_.whereaboutsObserverAdd(this);
		whereaboutsManager_.connect();
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[ARLayout] Connected");	
	}

	public synchronized void disconnect() {
		if(whereaboutsManager_ != null) {
			whereaboutsManager_.whereaboutsObserverRem(this);
			whereaboutsManager_.disconnect();
		}
		
		Log.v(ConfigurationManager.LOGGING_TAG, "[ARLayout] Disconnected");
	}
	
	public synchronized void freeze() {
		previewState_ = PreviewState.FROZEN;
	}
	
	public synchronized void unfreeze() {
		previewState_ = PreviewState.FLUID;
	}

	public synchronized Mode mode() {
		return mode_;
	}

	public synchronized void modeIs(Mode _mode) {
		mode_ = _mode;
	}

	public void onDraw(Canvas _canvas) {
		Log.d(ConfigurationManager.LOGGING_TAG, "Drawing view");
		
		// Print debug information
		if (mode_ == Mode.DEBUG) {
			// Take snapshot
			Location deviceLocation;
			Azimuth deviceAzimuth;
			Inclination deviceInclination;
			
			synchronized(this) {
				deviceLocation = new Location(deviceLocation_);
				deviceAzimuth = deviceAzimuth_;
				deviceInclination = deviceInclination_;
			}
			
			Paint p = new Paint();
			p.setColor(Color.WHITE);
			_canvas.drawText("Azimuth: " + deviceAzimuth + "     " + "Inclination: " + deviceInclination, 10, 20, p);
			_canvas.drawText("Location: lat=" + deviceLocation.getLatitude() + ", long=" + deviceLocation.getLongitude() + ", alt=" + deviceLocation.getAltitude(), 10, (int)screenHeight_ - 10, p);
		}
		
		drawOverlays(_canvas);
	}

	@Override
	public void whereaboutsChanged(Location _location, Azimuth _azimuth,
			Inclination _inclination) {
		synchronized(this) {
			if(previewState_ == PreviewState.FLUID) {
				deviceLocation_ = new Location(_location);
				deviceAzimuth_ = _azimuth;
				deviceInclination_ = _inclination;
			}
		}
		
		postInvalidate();
	}
	
	public void singlePress(double _x, double _y) {
		Overlay candidate = findOverlayCandidate(_x, _y);
		if(candidate != null) {
			candidate.highlight();
			
			synchronized(this) {
				hasHighlighted_ = true;
			}
		}
	}
	
	public void singleClick(double _x, double _y) {
		Overlay candidate = findOverlayCandidate(_x, _y);
		if(candidate != null) {
			candidate.singleClick();
		}
		
		synchronized(this) {
			hasHighlighted_ = false;
		}
	}
	
	public void longClick(double _x, double _y) {
		Overlay candidate = findOverlayCandidate(_x, _y);
		if(candidate != null) {
			candidate.longClick();
		}
		
		synchronized(this) {
			hasHighlighted_ = false;
		}
	}
	
	private Overlay findOverlayCandidate(double _x, double _y) {
		// Take snapshot
		Location deviceLocation;
		Azimuth deviceAzimuth;
		Inclination deviceInclination;
		
		synchronized(this) {
			deviceLocation = new Location(deviceLocation_);
			deviceAzimuth = deviceAzimuth_;
			deviceInclination = deviceInclination_;
		}
		
		for(Overlay overlay : overlays_.overlays(deviceLocation, deviceAzimuth, deviceInclination)) {			
			int centerHorizontal = overlay.getLeft();
			int centerVertical = overlay.getTop();
			
			int width = (int) (overlay.width() * (1 + TOUCH_ERROR_MARGIN));
			int height = (int) (overlay.height() * (1 + TOUCH_ERROR_MARGIN));
			
			int left = (int) (centerHorizontal - width / 2);
			int top = (int) (centerVertical - height / 2);
			int right = (int) (centerHorizontal + width / 2);
			int bottom = (int) (centerVertical + height / 2);
			
			if(left <= _x && _x <= right && top <= _y && _y <= bottom) {
				return overlay;
			}
		}
		
		return null;
	}

	private void drawOverlays(Canvas _canvas) {
		Log.d(ConfigurationManager.LOGGING_TAG, "Drawing overlays");
		
		// Take snapshot
		Location deviceLocation;
		Azimuth deviceAzimuth;
		Inclination deviceInclination;
		double screenWidth;
		double screenHeight;
		
		synchronized(this) {
			deviceLocation = new Location(deviceLocation_);
			deviceAzimuth = deviceAzimuth_;
			deviceInclination = deviceInclination_;
			screenWidth = screenWidth_;
			screenHeight = screenHeight_;
		}
		
		Log.d(ConfigurationManager.LOGGING_TAG, "Snapshot taken");
		
		Log.d(ConfigurationManager.LOGGING_TAG, "Starting to draw overlays");
		
		overlays_.updateOverlays(deviceLocation);
		
		// Draw overlays
		for(Overlay overlay : overlays_.overlays(deviceLocation, deviceAzimuth, deviceInclination)) {
			// Unhighlight overlays
			if(!hasHighlighted_) {
				overlay.unhighlight();
			}
			
			// Compute the overlay's position
			Triple<Distance, Azimuth, Inclination> overlayValues = overlay.values();
			
			int left = computeHorizontalPosition(overlay, screenWidth, deviceAzimuth, overlayValues._2);
			int top = computeVerticalPosition(overlay, screenHeight, deviceInclination, overlayValues._3);
			int right = overlay.getRight();
			int bottom = overlay.getBottom();
			
			// Assign a position to the overlay
			overlay.layout(left, top, right, bottom);
			
			// Draw the overlay
			overlay.onDraw(_canvas);
		}
		
		Log.d(ConfigurationManager.LOGGING_TAG, "Done drawing overlays");
	}
	
	private int computeHorizontalPosition(Overlay _overlay, double _screenWidth, Azimuth _deviceAzimuth, Azimuth _overlayAzimuth) {
		RelativeAzimuth relativeAzimuth = _deviceAzimuth.azimuthTo(_overlayAzimuth);
		
		double halfScreenWidth = _screenWidth / 2.0;
		
		// We reverse this because the offset should be in the opposite direction on screen
		double horizontalScreenOffset = -(relativeAzimuth.doubleValue() / xAngleWidth_) * halfScreenWidth;
		
		return (int) (halfScreenWidth + horizontalScreenOffset);
	}
	
	private int computeVerticalPosition(Overlay _overlay, double _screenHeight, Inclination _deviceInclination, Inclination _overlayInclination) {
		RelativeInclination relativeInclination = _deviceInclination.inclinationTo(_overlayInclination);
		
		double halfScreenHeight = _screenHeight / 2.0;
		
		// We don't reverse this because the offset should be in the same direction on screen
		double verticalScreenOffset = (relativeInclination.doubleValue() / yAngleWidth_) * halfScreenHeight;
		
		return (int) (halfScreenHeight + verticalScreenOffset);
	}

	private void valid() {
		if(xAngleWidth_ <= 0.0) {
			throw new RuntimeException("Horizontal view angle should not be negative or zero!");
		}
		
		if(yAngleWidth_ <= 0.0) {
			throw new RuntimeException("Vertical view angle should not be negative or zero!");
		}
	}

}