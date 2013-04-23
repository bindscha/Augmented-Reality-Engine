package com.b01dface.arlo.overlays;

import java.util.ArrayList;

import com.b01dface.arengine.overlays.Overlay;
import com.b01dface.arengine.overlays.OverlayCollection;
import com.b01dface.arengine.overlays.OverlayDraw;
import com.b01dface.arengine.overlays.SimpleOverlayDraw;

import android.content.Context;
import android.graphics.Color;

/**
 * <code>CalendarAdapter</code> adapts calendar items to overlays.
 * 
 * @author Laurent Bindschaedler
 */
public class CalendarAdapter {

	private static final OverlayDraw CALENDAR_OVERLAY_DRAW = new SimpleOverlayDraw(Color.RED, Color.WHITE, Color.RED);
	
	private final Context context_;
	
	public CalendarAdapter(Context _context) {
		context_ = _context;
	}
	
	public OverlayCollection overlays() {
		ArrayList<Overlay> overlays = new ArrayList<Overlay>();
		
		CalendarService calendarService = new CalendarService(context_);
		
		for(CalendarEvent event : calendarService.eventsWithLocation()) {
			EventEntity entity = new EventEntity(event);
			
			Overlay overlay = new Overlay(context_, entity, CALENDAR_OVERLAY_DRAW);
			
			overlays.add(overlay);
		}
		
		return new OverlayCollection(overlays);
	}
	
}
