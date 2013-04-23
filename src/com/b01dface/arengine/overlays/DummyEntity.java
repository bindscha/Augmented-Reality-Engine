package com.b01dface.arengine.overlays;

import android.location.Location;

/**
 * <code>DummyEntity</code> is a dummy entity class.
 * 
 * @author Laurent Bindschaedler
 */
public class DummyEntity extends Entity {

	private String title_;
	private String type_;
	private String description_;
	private Location location_;
	
	public DummyEntity(String _title, String _type, String _description, Location _location) {
		super();
		title_ = _title;
		type_ = _type;
		description_ = _description;
		location_ = new Location(_location);
	}

	@Override
	public String title() {
		return title_;
	}

	@Override
	public String type() {
		return type_;
	}

	@Override
	public String description() {
		return description_;
	}

	@Override
	public Location location() {
		return new Location(location_);
	}

	@Override
	public void titleIs(String _title) {
		title_ = _title;
	}

	@Override
	public void typeIs(String _type) {
		type_ = _type;
	}

	@Override
	public void descriptionIs(String _description) {
		description_ = _description;
	}

	@Override
	public void locationIs(Location _location) {
		location_ = _location;
	}

}
