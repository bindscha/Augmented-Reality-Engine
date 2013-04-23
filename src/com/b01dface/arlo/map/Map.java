package com.b01dface.arlo.map;

import com.b01dface.arengine.ConfigurationManager;
import com.b01dface.arlo.core.R;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * <code>Map</code> corresponds to the map activity used in ARLO.
 * 
 * @author Laurent Bindschaedler
 */
public class Map extends MapActivity {
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.map);
		
		MapView mapView = (MapView) findViewById(R.id.MapView);
		mapView.setBuiltInZoomControls(true);
		
		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.clear();
		
		Drawable drawable1 = this.getResources().getDrawable(R.drawable.map_overlay_green);
		MapItemizedOverlay itemizedoverlay1 = new MapItemizedOverlay(drawable1, this);
		
		Location deviceLocation = ConfigurationManager.instance().deviceCurrentLocation();
		GeoPoint devicePoint = new GeoPoint((int)(deviceLocation.getLatitude() * 1E6), (int)(deviceLocation.getLongitude() * 1E6));
		OverlayItem deviceOverlayitem = new OverlayItem(devicePoint, "You", "This item represents your current position.");
		
		itemizedoverlay1.addOverlay(deviceOverlayitem);
		mapOverlays.add(itemizedoverlay1);
		
		Drawable drawable2 = this.getResources().getDrawable(R.drawable.map_overlay_blue);
		Drawable drawable3 = this.getResources().getDrawable(R.drawable.map_overlay_red);
		MapItemizedOverlay itemizedoverlay2 = new MapItemizedOverlay(drawable2, this);
		MapItemizedOverlay itemizedoverlay3 = new MapItemizedOverlay(drawable3, this);
		
		for(com.b01dface.arengine.overlays.Overlay overlay : ConfigurationManager.instance().overlayCollection().overlays()) {
			Location location = overlay.entity().location();
			GeoPoint point = new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6));
			OverlayItem overlayitem = new OverlayItem(point, overlay.entity().title(), overlay.entity().description());
			
			if(overlay.entity().type().equals("calendar event")) {
				itemizedoverlay3.addOverlay(overlayitem);
				mapOverlays.add(itemizedoverlay3);
			} else {
				itemizedoverlay2.addOverlay(overlayitem);
				mapOverlays.add(itemizedoverlay2);
			}
		}
		
		MapController controller = mapView.getController();
		Location center = new Location(deviceLocation);
		GeoPoint centerPoint = new GeoPoint((int)(center.getLatitude() * 1E6), (int)(center.getLongitude() * 1E6));
		
		controller.animateTo(centerPoint);
		controller.setZoom(20);
		mapView.postInvalidate();
	}

}
