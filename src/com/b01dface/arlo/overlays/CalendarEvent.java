package com.b01dface.arlo.overlays;

import java.util.Date;

import com.b01dface.arengine.ConfigurationManager;

import android.location.Location;

/**
 * <code>CalendarEvent</code> corresponds an Android calendar event
 * 
 * @author Laurent Bindschaedler
 */
public class CalendarEvent {
	
	public static final String LOCATION_PROVIDER = "calendar";
	
	private final long id_;
    private final Date beginDate_;
    private final Date endDate_;
    
    private final String title_;
    private final String description_;
    
    private final Location location_;
	
    public CalendarEvent(long _id, Date _beginDate, Date _endDate, String _title, String _description, Location _location) {
		id_ = _id;
		beginDate_ = new Date(_beginDate.getTime());
		endDate_ = new Date(_endDate.getTime());
		title_ = _title;
		description_ = _description;
		
		if(_location == null) {
			location_ = null;
		} else {
			location_ = new Location(_location);
		}
		
		valid();
	}

	public CalendarEvent(long _id, long _beginDate, long _endDate, String _title, String _description, String _location) {
		this(_id, dateFromLong(_beginDate), dateFromLong(_endDate), _title, _description, locationFromString(_location));
	}

	public CalendarEvent(CalendarEvent _event) {
		this(_event.id_, _event.beginDate_, _event.endDate_, _event.title_, _event.description_, _event.location_);
	}

	public long id() {
		return id_;
	}

	public Date beginDate() {
		return beginDate_;
	}
	
	public Date endDate() {
		return endDate_;
	}

	public String title() {
		return title_;
	}
	
	public String description() {
		return description_;
	}

	public Location location() {
		return location_;
	}

	@Override
	public String toString() {
		return "[Calendar Event] ID=" + id_ + ", title=" + title_ + ", description=" + description_ + ", location=" + location_ + ", start=" + beginDate_.toLocaleString() + ", end=" + endDate_.toLocaleString() + "";
	}
	
	private static Date dateFromLong(long _date) {
		return new Date(_date);
	}
	
	private static Location locationFromString(String _location) {
		return ConfigurationManager.instance().locationFromString(_location, LOCATION_PROVIDER);
	}
	
	private void valid() {
		if(id_ < 0) {
			throw new RuntimeException("CalendarEvent id cannot be negative!");
		}
		
		if(beginDate_ == null) {
			throw new RuntimeException("CalendarEvent begin date cannot be null!");
		}
		
		if(endDate_ == null) {
			throw new RuntimeException("CalendarEvent end date cannot be null!");
		}
		
		if(beginDate_.after(endDate_)) {
			throw new RuntimeException("CalendarEvent begin date cannot be after end date!");
		}
		
		if(title_ == null) {
			throw new RuntimeException("CalendarEvent title cannot be null!");
		}
	}
    
}
