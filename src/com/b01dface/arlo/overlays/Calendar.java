package com.b01dface.arlo.overlays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.b01dface.arengine.ConfigurationManager;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;

/**
 * <code>Calendar</code> is an Android calendar wrapper.
 * 
 * @author Laurent Bindschaedler
 */
public class Calendar implements Iterable<CalendarEvent> {
	
	private final Context context_;
	
	private final long calendarId_;
	private final String calendarName_;
	private final List<CalendarEvent> events_;
	
	public Calendar(Context _context, long _calendarId) {
		this(_context, _calendarId, null);
	}
	
	public Calendar(Context _context, long _calendarId, String _calendarName) {
		context_ = _context;
		
		events_ = new ArrayList<CalendarEvent>();
		
		calendarId_ = _calendarId;
		calendarName_ = _calendarName;
		
		ContentResolver contentResolver = context_.getContentResolver();
		
		Uri.Builder builder = Uri.parse("content://" + ConfigurationManager.instance().calendarProvider() + "/instances/when").buildUpon();
		
        long now = new Date().getTime();
        ContentUris.appendId(builder, now);
        ContentUris.appendId(builder, now + DateUtils.DAY_IN_MILLIS);
        Cursor eventCursor = contentResolver.query(builder.build(),
        		new String[] { "title", "description", "begin", "end", "eventLocation"}, "Calendars._id=" + calendarId_,
        		null, "startDay ASC, startMinute ASC");
        
        int i = 0;
        
        while (eventCursor.moveToNext()) {
        	String title = eventCursor.getString(0);
        	String description = eventCursor.getString(1);
        	long beginDate = eventCursor.getLong(2);
        	long endDate = eventCursor.getLong(3);
        	String location = eventCursor.getString(4);
        	
        	if(location == null || location.trim().length() == 0) {
        		location = null;
        	}
        	
        	CalendarEvent event = new CalendarEvent(i, beginDate, endDate, title, description, location);
        	
        	events_.add(event);
        	
        	Log.e("CalendarTest", "Title: " + title + " Begin: " + beginDate + " End: " + endDate +
        			" Description: " + description + " Location:" + location);
        	
        	++i;
        }
	}
	
	public long calendarId() {
		return calendarId_;
	}
	
	public String calendarName() {
		return calendarName_;
	}
	
	public CalendarEvent event(int _id) {
		return events_.get(_id);
	}

	public Collection<CalendarEvent> events() {
		return Collections.unmodifiableCollection(events_);
	}
	
	@Override
	public Iterator<CalendarEvent> iterator() {
		return Collections.unmodifiableList(events_).iterator();
	}
	
	

}
