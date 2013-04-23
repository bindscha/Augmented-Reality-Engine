package com.b01dface.arlo.overlays;

import android.location.Location;

import com.b01dface.arengine.overlays.Entity;

/**
 * <code>EventEntity</code> is the entity for a calendar event.
 * 
 * @author Laurent Bindschaedler
 */
public class EventEntity extends Entity {
	
	private static final String TYPE = "calendar event";

	private CalendarEvent event_;
	
	public EventEntity(CalendarEvent _event) {
		event_ = _event;
	}
	
	@Override
	public String title() {
		return event_.title();
	}

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public String description() {
		String beginDate = event_.beginDate().toLocaleString();
		String endDate = event_.endDate().toLocaleString();
		
		String description = event_.description();
		if(description == null) {
			description = "";
		}
		
		return 	"Start: " + beginDate + "\n" + 
				"End: " + endDate + "\n" + 
				"Description: " + description;
	}

	@Override
	public Location location() {
		return event_.location();
	}

	@Override
	public void titleIs(String _title) {
		// TODO
	}

	@Override
	public void typeIs(String _type) {
		// TODO
	}

	@Override
	public void descriptionIs(String _description) {
		// TODO
	}

	@Override
	public void locationIs(Location _location) {
		// TODO
	}

}
