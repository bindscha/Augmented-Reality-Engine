package com.b01dface.arlo.overlays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.b01dface.arengine.ConfigurationManager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Wrapper for the calendar service.
 * 
 * @author Laurent Bindschaedler
 */
public class CalendarService {
	
	private final Context context_;
	
	private final Map<Long, Calendar> calendars_;
	
	public CalendarService(Context _context) {
		context_ = _context;
		
		calendars_ = new HashMap<Long, Calendar>();
		
		ContentResolver contentResolver = context_.getContentResolver();
		
        /*final Cursor cursor = contentResolver.query(Uri.parse("content://" + ConfigurationManager.instance().calendarProvider() + "/calendars"),
        		(new String[] { "_id", "displayName", "selected" }), null, null, null);
        
        while (cursor.moveToNext()) {
        	final long id = cursor.getLong(0);
        	final String name = cursor.getString(1);
        	final boolean selected = !cursor.getString(2).equals("0");
        	
        	if(selected) {
	        	Calendar calendar = new Calendar(context_, id);
	        	calendars_.put(id, calendar);
        	}
        	
        	Log.e("Test", "Id: " + id + " Display Name: " + name + " Selected: " + selected);
        }*/
	}

	public Collection<CalendarEvent> events() {
		ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
		
		for(Calendar calendar : calendars_.values()) {
			events.addAll(calendar.events());
		}
		
		return events;
	}
	
	public Collection<CalendarEvent> eventsWithLocation() {
		ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
		
		for(Calendar calendar : calendars_.values()) {
			
			for(CalendarEvent event : calendar) {
				if(event.location() != null) {
					events.add(event);
				}
			}
			
		}
		
		return events;
	}
	
}
