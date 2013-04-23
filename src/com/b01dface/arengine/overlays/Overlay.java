package com.b01dface.arengine.overlays;

import java.util.UUID;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.location.Location;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.b01dface.arengine.ConfigurationManager;
import com.b01dface.arengine.util.Azimuth;
import com.b01dface.arengine.util.Distance;
import com.b01dface.arengine.util.Inclination;
import com.b01dface.arengine.util.Triple;
import com.b01dface.arlo.core.R;
import com.b01dface.arlo.map.Map;

/**
 * <code>Overlay</code> is the standard class used by the ARE for overlays.
 * 
 * @author Laurent Bindschaedler
 */
public final class Overlay extends View {

	private static final SimpleOverlayDraw DEFAULT_OVERLAY_DRAW = new SimpleOverlayDraw();
	
	private final Context context_;
	
	private final Entity entity_;
	private final OverlayDraw draw_;
	
	private Distance distance_;
	private Azimuth azimuth_;
	private Inclination inclination_;
	
	private final View.OnClickListener clickListener_ = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Toast.makeText(context_, R.string.gps_status_available, Toast.LENGTH_SHORT).show();
		}
	};
	
	private final View.OnLongClickListener longClickListener_ = new View.OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			Toast.makeText(context_, R.string.gps_status_out_of_service, Toast.LENGTH_SHORT).show();
			return true;
		}
	};
	
	public Overlay(Context _context, Entity _entity) {
		this(_context, _entity, DEFAULT_OVERLAY_DRAW);
	}
	
	public Overlay(Context _context, Entity _entity, Location _deviceLocation) {
		this(_context, _entity, DEFAULT_OVERLAY_DRAW, _deviceLocation);
	}
	
	public Overlay(Context _context, Entity _entity, OverlayDraw _draw) {
		this(_context, _entity, _draw, null);
	}
	
	public Overlay(Context _context, Entity _entity, OverlayDraw _draw, Location _deviceLocation) {
		super(_context);
		
		context_ = _context;
		
		entity_ = _entity;
		draw_ = _draw;
		
		distance_ = ConfigurationManager.DEFAULT_DISTANCE;
		azimuth_ = ConfigurationManager.DEFAULT_AZIMUTH;
		inclination_ = ConfigurationManager.DEFAULT_INCLINATION;
		
		if(_deviceLocation != null) {
			updateValues(_deviceLocation);
		}
		
		setOnClickListener(clickListener_);
		setOnLongClickListener(longClickListener_);
		
		valid();
	}
	
	public String shortTitle() {
		return entity_.shortTitle();
	}
	
	public UUID id() {
		return entity_.id();
	}
	
	public Entity entity() {
		return entity_;
	}
	
	public int width() {
		return draw_.width();
	}
	
	public int height() {
		return draw_.height();
	}
	
	public void highlight() {
		draw_.highlight();
		
		invalidate();
	}
	
	public void unhighlight() {
		draw_.unhighlight();
		
		invalidate();
	}
	
	public void singleClick() {
		final Dialog singleClickDialog = new Dialog(context_);

		singleClickDialog.setContentView(R.layout.overlay_dialog);
		singleClickDialog.setTitle(entity_.title());
		
		ImageView overlayImage = (ImageView) singleClickDialog.findViewById(R.id.overlayImage);
		overlayImage.setImageResource(iconResource());
		
		TextView overlayText = (TextView) singleClickDialog.findViewById(R.id.overlayText);
		overlayText.setText(entity_.description());
		
		Button dismissButton = (Button) singleClickDialog.findViewById(R.id.dismiss);
		dismissButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				singleClickDialog.dismiss();
			}
		});
		
		Button mapButton = (Button) singleClickDialog.findViewById(R.id.showOnMap);
		mapButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				singleClickDialog.dismiss();
				Intent intent = new Intent(context_, Map.class);
				context_.startActivity(intent);
			}
		});
		
		singleClickDialog.show();
	}
	
	public void longClick() {
		if(entity_.type().equals("calendar event")) {
			Intent i = new Intent(Intent.ACTION_MAIN);
	        i.setComponent(new ComponentName("com.htc.calendar", "com.htc.calendar.DayActivity"));
	        context_.startActivity(i);
		}
	}
	
	private int iconResource() {
		if(entity_.type().equals("calendar event")) {
			return R.drawable.calendar;
		} else {
			return R.drawable.latitude;
		}
	}
	
	public synchronized void updateValues(Location _deviceLocation) {
		if(_deviceLocation != null) {
			Location entityLocation = entity_.location();
			
			// We have to be careful here...
			// 	 distance is relative (i.e. distance between two positions on the map)
			//   azimuth is absolute (it's a bearing)
			//   inclination is absolute (even though it's the angle between two relative distances)
			double distance = _deviceLocation.distanceTo(entityLocation);
			double azimuth = _deviceLocation.bearingTo(entityLocation);
			double inclination = 0.0;
			
			// Inclination requires altitude
			if(_deviceLocation.hasAltitude() && entityLocation.hasAltitude()) {
				if(distance == 0.0) {
					// Don't compute with distance 0 or else the thing will blow up sky high...
					inclination = 0.0;
				} else {
					// arctan(altitude/distance) yields the correct result by the theorem of reverse angles
					double deltaAltitude = entityLocation.getAltitude() - _deviceLocation.getAltitude();
					inclination = Math.atan(deltaAltitude / distance);
				}
				
			} else {
				inclination = 0.0;
			}
			
			distance_ = new Distance(distance);
			azimuth_ = new Azimuth(azimuth);
			inclination_ = new Inclination(inclination);
		}
	}
	
	public synchronized Triple<Distance, Azimuth, Inclination> values() {
		return new Triple<Distance, Azimuth, Inclination>(distance_, azimuth_, inclination_);
	}
	
	@Override
	public void onDraw(Canvas _canvas) {
		draw_.draw(_canvas, this);
	}
	
	@Override
	public int hashCode() {
		return entity_.hashCode();
	}

	@Override
	public boolean equals(Object _object) {
		if (this == _object)
			return true;
		if (_object == null)
			return false;
		if (getClass() != _object.getClass())
			return false;
		Overlay other = (Overlay) _object;
		return entity_.equals(other.entity_);
	}

	private void valid() {
		if(entity_ == null) {
			throw new RuntimeException("Entity cannot be null!");
		}
		
		if(distance_ == null) {
			throw new RuntimeException("Distance cannot be null!");
		}
		
		if(azimuth_ == null) {
			throw new RuntimeException("Azimuth cannot be null!");
		}
		
		if(inclination_ == null) {
			throw new RuntimeException("Inclination cannot be null!");
		}
	}
	
}
