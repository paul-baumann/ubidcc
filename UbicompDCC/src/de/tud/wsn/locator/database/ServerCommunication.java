package de.tud.wsn.locator.database;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

public class ServerCommunication extends AsyncTask<String, Void, String>{
	
	@Override
	protected String doInBackground(String... params) {
		String urlPath = params[0];
		String data = params[1];
	
		try {
			HttpURLConnection conn = null;
			URL url = new URL(urlPath);

			conn = (HttpURLConnection) url.openConnection();
//			// Allow Inputs
//			conn.setDoInput(true);
//			// Allow Outputs
//			conn.setDoOutput(true);
//			// Don't use a cached copy.
//			conn.setUseCaches(false);
//			// set timeout
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);
			// Use a post method.
			conn.setRequestProperty("Content-Length", String.valueOf(data.length()));
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			// Stream
			DataOutputStream output = new DataOutputStream(conn.getOutputStream());
			output.writeBytes(data);
			output.flush();
			output.close();

			InputStream content = conn.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(content));
			StringBuffer html = new StringBuffer();
			String line;
			while ((line = in.readLine()) != null) {
				html.append(line);
			}
			String text = html.toString();
			Log.e("User", text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
