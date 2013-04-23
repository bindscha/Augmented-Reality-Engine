package com.b01dface.arlo.core;

import com.b01dface.arengine.Engine;
import com.b01dface.arlo.map.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * This is the <code>Activity</code> for the ARLO application.
 * 
 * @author Laurent Bindschaedler
 */
public class MainActivity extends Engine {

	public void onStart() {
		super.onStart();
	}

	public void onCreate(Bundle savedInstanceState) {
		try {
			if(savedInstanceState == null) {
				savedInstanceState = new Bundle();
			}
			savedInstanceState.putBoolean("debug", false);
			
			super.onCreate(savedInstanceState);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.map:
	        map();
	        return true;
	    case R.id.list:
	        list();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	private void list() {
		// TODO 
	}

	private void map() {
		Intent intent = new Intent(MainActivity.this, Map.class);
		MainActivity.this.startActivity(intent);
	}
	
}