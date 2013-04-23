package com.b01dface.arengine.overlays;

import com.b01dface.arengine.ConfigurationManager;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;

// TODO: adjust this so that size values make sense with different screen resolutions
/**
 * <code>SimpleOverlayDraw</code> is an example overlay draw class.
 * 
 * @author Laurent Bindschaedler
 */
public class SimpleOverlayDraw implements OverlayDraw {
	
	private static final int DEFAULT_CIRCLE_RADIUS = 20;
	private static final int DEFAULT_TEXT_SIZE = 14;
	
	private static final int DEFAULT_STROKE_WIDTH = 2;
	private static final int DEFAULT_HIGHLIGHTED_STROKE_WIDTH = 5;
	
	private final Paint fillPaint_;
	private final Paint strokePaint_;
	private final Paint textPaint_;
	
	private final int circleRadius_;
	private final int textSize_;
	
	private int strokeWidth_;
	
	public SimpleOverlayDraw() {
		this(Color.BLUE, Color.WHITE, Color.BLUE);
	}
	
	public SimpleOverlayDraw(int _overlayColor, int _strokeColor, int _textColor) {
		double scaleFactor = ConfigurationManager.instance().scaleFactor();
		
		circleRadius_ = (int) (DEFAULT_CIRCLE_RADIUS * scaleFactor);
		textSize_ = (int) (DEFAULT_TEXT_SIZE * scaleFactor);
		
		strokeWidth_ = DEFAULT_STROKE_WIDTH;
		
		// Fill paint
		fillPaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		fillPaint_.setStyle(Paint.Style.FILL);
		fillPaint_.setColor(_overlayColor);
		fillPaint_.setAlpha(150);
		
		// Stroke paint
		strokePaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		strokePaint_.setStyle(Paint.Style.STROKE);
		strokePaint_.setColor(_strokeColor);
		strokePaint_.setAlpha(255);
		
		strokePaint_.setStrokeWidth(strokeWidth_);
		
		// Text paint
		textPaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint_.setColor(_textColor);
		textPaint_.setAlpha(150);
		
		textPaint_.setTextSize(textSize_);
		textPaint_.setTextAlign(Align.CENTER);
	}
	
	@Override
	public void draw(Canvas _canvas, Overlay _overlay) {
		// Draw circle with stroke
		_canvas.drawCircle(_overlay.getLeft(), _overlay.getTop(), circleRadius_, fillPaint_);
		_canvas.drawCircle(_overlay.getLeft(), _overlay.getTop(), circleRadius_, strokePaint_);

		// Draw text
		_canvas.drawText(_overlay.shortTitle(), _overlay.getLeft(), _overlay.getTop() + (int)(1.5 * circleRadius_ + textSize_), textPaint_);
	}
	
	public int width() {
		return circleRadius_ * 3;
	}
	
	public int height() {
		return circleRadius_ * 3;
	}

	@Override
	public void highlight() {
		strokeWidth_ = DEFAULT_HIGHLIGHTED_STROKE_WIDTH;
		strokePaint_.setStrokeWidth(strokeWidth_);
	}

	@Override
	public void unhighlight() {
		strokeWidth_ = DEFAULT_STROKE_WIDTH;
		strokePaint_.setStrokeWidth(strokeWidth_);
	}

}
