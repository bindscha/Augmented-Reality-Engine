package com.b01dface.arengine.overlays;

import android.graphics.Canvas;

/**
 * <code>OverlayDraw</code> is the interface used to define the looks of an overlay.
 * 
 * @author Laurent Bindschaedler
 */
public interface OverlayDraw {

	public void draw(Canvas _canvas, Overlay _overlay);
	
	public int width();
	
	public int height();
	
	public void highlight();
	
	public void unhighlight();
	
}
