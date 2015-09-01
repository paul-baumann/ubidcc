package de.tud.wsn.locator.util;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class HelperObjects {

	public static class LocationObject {

		public String name;
		public Double timestamp;
		@SerializedName("mLatitude")
		public Double latitude;
		@SerializedName("mLongitude")
		public Double longitude;
		@SerializedName("mAccuracy")
		public Double accuracy;
		@SerializedName("mTime")
		public Long time;
		@SerializedName("mProvider")
		public String provider;
		@SerializedName("mExtras")
		public Map<String, Object> extras = new LinkedHashMap<String, Object>();


		public LocationObject() {

		}


		public String toString() {

			return "Longitude: " + longitude + " Latitude: " + latitude;
		}


		public String getProviderDetail() {

			String providerDetail;
			if (extras != null)
				providerDetail = (String) extras.get("networkLocationType");
			else
				providerDetail = "";

			if (provider.equals("network") && providerDetail.equals("cell"))
				return "Cell";
			else if (provider.equals("network") && providerDetail.equals("wifi"))
				return "Wifi";
			else if (provider.equals("gps"))
				return "GPS";
			else
				return "Unknown";
		}


		public Double getLatitude() {

			return latitude;
		}


		public Double getLongitude() {

			return longitude;
		}


		public Long getTimestamp() {

			return time;
		}

	}

	public static class WifiObject {

		String SSID;
		String BSSID;


		public WifiObject() {

		}

	}

	public static class BluetoothObject {

	}

	public static class CellTowerObject {

	}

	public static class ScreenObject {

	}

	public static class BatteryObject {

	}

	public static class ActivityObject {

	}

	public static class CallLogObject {

	}

	public static class MarkerObject {

		public Integer Id;
		public String title;
		public String snippet;
		public Double longitude;
		public Double latitude;
		public long timestamp;


		public MarkerObject() {

		}
	}

	public static class LogObject {

		public boolean wlan;
		public boolean bluetooth;
		public boolean gps;
		public boolean internet;
		public String additional;


		public LogObject() {

		}
	}
}
