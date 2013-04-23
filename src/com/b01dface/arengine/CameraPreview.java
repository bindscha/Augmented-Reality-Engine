package com.b01dface.arengine;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * The <code>CameraPreview<code> displays what the camera sees within a surface.
 * <BR>
 * This class also handles the freezing and unfreezing of the view.
 * 
 * @author Laurent Bindschaedler
 */
public class CameraPreview extends SurfaceView {
	
	/** Camera Preview State (frozen, fluid) */
	public static enum PreviewState {
		FLUID, FROZEN;
	}
	
	public static final float PICTURE_SIZE_FACTOR = 2.0f;
	public static final int MINIMUM_STATE_CHANGE_DELAY = 500;
	
	private Camera camera_;
	private SurfaceHolder previewHolder_;
	
	private byte[] lastPictureData_;
	
	private PreviewState previewState_;
	private long lastStateChangeTime_;

	// Callback for the surfaceholder
	private SurfaceHolder.Callback surfaceHolderListener_ = new SurfaceHolder.Callback() {
		
		public void surfaceCreated(SurfaceHolder _surfaceHolder) {
			synchronized(CameraPreview.this) {			
				camera_ = Camera.open();
	
				try {
					camera_.setPreviewDisplay(previewHolder_);
				} catch (Throwable t) {
					// ignored
				}
			}
		}

		public void surfaceChanged(SurfaceHolder _surfaceHolder, int _format, int _width, int _height) {
			synchronized(CameraPreview.this) {
				if(camera_ != null) {
					Parameters parameters = camera_.getParameters();
					
					parameters.setPreviewFormat(PixelFormat.JPEG);
					
					List<Size> possiblePictureSizes = parameters.getSupportedPictureSizes();
			        Size optimalPictureSize = findOptimalSize(possiblePictureSizes, (int)(_width * PICTURE_SIZE_FACTOR), (int)(_height * PICTURE_SIZE_FACTOR));
			        parameters.setPictureSize(optimalPictureSize.width, optimalPictureSize.height);
			        
			        List<Size> possiblePreviewSizes = parameters.getSupportedPreviewSizes();
			        Size optimalPreviewSize = findOptimalSize(possiblePreviewSizes, (int)(_width * PICTURE_SIZE_FACTOR), (int)(_height * PICTURE_SIZE_FACTOR));
			        parameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
					
					//camera_.setParameters(parameters);
					camera_.startPreview();
					
					camera_.setPreviewCallback(cameraPreviewCallback_);
				}
			}
		}

		public void surfaceDestroyed(SurfaceHolder _surfaceHolder) {
			synchronized(CameraPreview.this) {				
				// Close camera
				CameraPreview.this.closeCamera();
			}
		}
		
		private Size findOptimalSize(List<Size> sizes, int w, int h) {
	        final double ASPECT_TOLERANCE = 0.05;
	        double targetRatio = (double) w / h;
	        if (sizes == null) return null;

	        Size optimalSize = null;
	        double minDiff = Double.MAX_VALUE;

	        int targetHeight = h;

	        // Try to find an size match aspect ratio and size
	        for (Size size : sizes) {
	            double ratio = (double) size.width / size.height;
	            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
	            if (Math.abs(size.height - targetHeight) < minDiff) {
	                optimalSize = size;
	                minDiff = Math.abs(size.height - targetHeight);
	            }
	        }

	        // Cannot find the one match the aspect ratio, ignore the requirement
	        if (optimalSize == null) {
	            minDiff = Double.MAX_VALUE;
	            for (Size size : sizes) {
	                if (Math.abs(size.height - targetHeight) < minDiff) {
	                    optimalSize = size;
	                    minDiff = Math.abs(size.height - targetHeight);
	                }
	            }
	        }
	        return optimalSize;
	    }

	};
	
	private Camera.PreviewCallback cameraPreviewCallback_ = new Camera.PreviewCallback() {
		
		@Override
		public void onPreviewFrame(byte[] _data, Camera _camera) {
			synchronized(CameraPreview.this) {
				if(_data != null) {
					lastPictureData_ = _data;
				}
			}
		}
		
	};

	public CameraPreview(Context _context) {
		this(_context, null);
	}

	public CameraPreview(Context _context, AttributeSet _attributeSet) {
		this(_context, _attributeSet, 0);
	}
	
	public CameraPreview(Context _context, AttributeSet _attributeSet, int _defaultStyle) {
		super(_context, _attributeSet, _defaultStyle);
		
		previewHolder_ = this.getHolder();
		previewHolder_.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		previewHolder_.addCallback(surfaceHolderListener_);
		setBackgroundColor(Color.TRANSPARENT);
		
		previewState_ = PreviewState.FLUID;
		lastStateChangeTime_ = 0;
	}

	public synchronized Bitmap pauseCamera() {
		// Check if we're within no state change period
		long deltaStateChangeTime = System.currentTimeMillis() - lastStateChangeTime_;
		
		// Enforce correct state when called
		if(camera_ != null && previewState_ == PreviewState.FLUID && deltaStateChangeTime >= MINIMUM_STATE_CHANGE_DELAY) {
			previewState_ = PreviewState.FROZEN;
			camera_.stopPreview();

			lastStateChangeTime_ = System.currentTimeMillis();
		}
		
		return lastPicture();
	}
	
	public synchronized void unpauseCamera() {
		// Check if we're within no state change period
		long deltaStateChangeTime = System.currentTimeMillis() - lastStateChangeTime_;
		
		// Enforce correct state when called
		if(camera_ != null && previewState_ == PreviewState.FROZEN && deltaStateChangeTime >= MINIMUM_STATE_CHANGE_DELAY) {
			previewState_ = PreviewState.FLUID;
			camera_.startPreview();
			
			lastStateChangeTime_ = System.currentTimeMillis();
		}
	}
	
	public synchronized PreviewState previewState() {
		return previewState_;
	}
	
	public synchronized Bitmap lastPicture() {
		Bitmap picture = null;
		
		if(lastPictureData_ != null) {
			picture = BitmapFactory.decodeByteArray(lastPictureData_, 0, lastPictureData_.length);
		}
		
		return picture;
	}

	public synchronized void closeCamera() {
		if (camera_ != null) {
			try {
				// Remove callback
				camera_.setPreviewCallback(null);
				
				camera_.stopPreview();
				camera_.release();
				camera_ = null;
			} catch (Throwable exception) {
				// ignored
			}
		}
	}

}
