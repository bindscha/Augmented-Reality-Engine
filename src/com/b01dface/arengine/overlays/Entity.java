package com.b01dface.arengine.overlays;

import java.util.UUID;

import android.location.Location;

/**
 * <code>Entity</code> is the superclass for all entities used in the ARE.
 * Entities correspond to any entity provided by a data source.
 * 
 * @author Laurent Bindschaedler
 */
public abstract class Entity {

	public static final int DEFAULT_SHORTENED_LENGTH = 9;
	public static final int LAST_CHARACTERS_LENGTH = 3;

	public static final String ELIPSES = "...";

	private static int shortenedLength_ = DEFAULT_SHORTENED_LENGTH;

	public static synchronized void shortenedLengthIs(int _shortenedLength) {
		if (_shortenedLength > 0) {
			shortenedLength_ = _shortenedLength;
		}
	}
	

	private final UUID id_;

	public Entity() {
		id_ = UUID.randomUUID();
		
		valid();
	}

	public final UUID id() {
		return id_;
	}

	public abstract String title();

	public final String shortTitle() {
		String title = title();
		int titleLength = title.length();
		if (titleLength <= shortenedLength_ + ELIPSES.length()) {
			return title;
		} else {
			return title.substring(0, shortenedLength_ - LAST_CHARACTERS_LENGTH)
					+ ELIPSES
					+ title.substring(titleLength - LAST_CHARACTERS_LENGTH, titleLength);
		}
	}

	public abstract String type();

	public abstract String description();

	public abstract Location location();
	
	public abstract void titleIs(String _title);
	
	public abstract void typeIs(String _type);

	public abstract void descriptionIs(String _description);

	public abstract void locationIs(Location _location);
	
	@Override
	public final int hashCode() {
		return id_.hashCode();
	}

	@Override
	public final boolean equals(Object _object) {
		if (this == _object)
			return true;
		if (_object == null)
			return false;
		if (getClass() != _object.getClass())
			return false;
		Entity other = (Entity) _object;
		if (id_ == null) {
			if (other.id_ != null)
				return false;
		} else if (!id_.equals(other.id_))
			return false;
		return true;
	}

	private final void valid() {
		if(id_ == null) {
			throw new RuntimeException("Entity cannot have a null ID!");
		}
	}

}
