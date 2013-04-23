package com.b01dface.arengine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.os.Build;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The <code>DeviceDatabase<code> is a singleton object containing device-related information
 * necessary for the AR display (e.g. view angles). This information is obtained from the <b>device_database.xml</b> file.
 * 
 * @author Laurent Bindschaedler
 */
public class DeviceDatabase {
	
	private static class DeviceDatabaseInner {
		
		public static final DeviceDatabase DEVICE_DATABASE = new DeviceDatabase();
		
	}
	
	public static class DeviceInformation {
		
		private final String manufacturer_, modelName_;
		private final double horizontalViewAngle_, verticalViewAngle_;
		
		public DeviceInformation(String _manufacturer, String _modelName, double _horizontalViewAngle, double _verticalViewAngle) {
			manufacturer_ = _manufacturer;
			modelName_ = _modelName;
			horizontalViewAngle_ = _horizontalViewAngle;
			verticalViewAngle_ = _verticalViewAngle;
		}

		public String manufacturer() {
			return manufacturer_;
		}

		public String modelName() {
			return modelName_;
		}

		public double horizontalViewAngle() {
			return horizontalViewAngle_;
		}

		public double verticalViewAngle() {
			return verticalViewAngle_;
		}
		
	}
	
	public static final double DEFAULT_HORIZONTAL_VIEW_ANGLE = 60.0;
	public static final double DEFAULT_VERTICAL_VIEW_ANGLE = 40.0;
	
	public static final DeviceInformation DEFAULT_DEVICE = new DeviceInformation("B01DFACE", "Default", DEFAULT_HORIZONTAL_VIEW_ANGLE, DEFAULT_VERTICAL_VIEW_ANGLE);

	private final Map<String, DeviceInformation> deviceDatabase_;
	
	private DeviceDatabase() {
		deviceDatabase_ = new HashMap<String, DeviceDatabase.DeviceInformation>();

		readDatabase();
	}
	
	public static DeviceDatabase instance() {
		return DeviceDatabaseInner.DEVICE_DATABASE;
	}
	
	public DeviceInformation deviceInformation() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;

		DeviceInformation deviceInformation = null;
		
		// First attempt - exact match
		deviceInformation = deviceDatabase_.get(manufacturer + " " + model);
		
		if(deviceInformation != null) {
			return deviceInformation;
		}
		
		// Second attempt - device names partially match
		for(DeviceInformation potentialDevice : deviceDatabase_.values()) {
			if(potentialDevice.modelName().indexOf(model) >= 0 || model.indexOf(potentialDevice.modelName()) >= 0) {
				// Return potential match
				return potentialDevice;
			}
		}
		
		return DEFAULT_DEVICE;
	}
	
	private void readDatabase() {
		try {
			Map<String, DeviceInformation> database = new HashMap<String, DeviceInformation>();
			
			InputStream databaseInputStream = ConfigurationManager.instance().deviceDatabaseInputStream();
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(databaseInputStream));
			doc.getDocumentElement().normalize();

			NodeList nodeList = doc.getElementsByTagName("device");
			
			for(int i = 0; i < nodeList.getLength(); ++i) {
				Node node = nodeList.item(i);

				Element element = (Element) node;
				String manufacturer = element.getElementsByTagName("manufacturer").item(0).getFirstChild().getNodeValue();
				String modelName = element.getElementsByTagName("model").item(0).getFirstChild().getNodeValue();
				String stringHorizontalViewAngle = element.getElementsByTagName("horizontalViewAngle").item(0).getFirstChild().getNodeValue();
				String stringVerticalViewAngle = element.getElementsByTagName("verticalViewAngle").item(0).getFirstChild().getNodeValue();
				double horizontalViewAngle = -1.0;
				double verticalViewAngle = -1.0;
				
				try {
					horizontalViewAngle = Double.parseDouble(stringHorizontalViewAngle);
					verticalViewAngle = Double.parseDouble(stringVerticalViewAngle);
				} catch (NumberFormatException e) {
					// ignored
				}
				
				if(manufacturer != null && modelName != null && horizontalViewAngle > 0.0 && verticalViewAngle > 0.0) {
					database.put(manufacturer + " " + modelName, new DeviceInformation(manufacturer, modelName, horizontalViewAngle, verticalViewAngle));
				}
			}
			
			// Copy to actual database if no problems occurred
			deviceDatabase_.putAll(database);
			
		} catch (FileNotFoundException e) {
			// ignored
		} catch (ParserConfigurationException e) {
			// ignored
		} catch (SAXException e) {
			// ignored
		} catch (IOException e) {
			// ignored
		}
	}

}
