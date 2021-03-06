package edu.mit.media.funf.probe.builtin;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import edu.mit.media.funf.probe.ProbeTestCase;
import edu.mit.media.funf.probe.Probe.Parameter;
import edu.mit.media.funf.tests.JsonUtils;

public class ProcessStatisticsProbeTest extends ProbeTestCase<ProcessStatisticsProbe> {

	public ProcessStatisticsProbeTest() {
		super(ProcessStatisticsProbe.class);
	}

	public void testData() {
		Bundle params = new Bundle();
		params.putLong(Parameter.Builtin.PERIOD.name, 10L);
		startProbe(params);
		
		Bundle data = getData(10);
		Gson gson = JsonUtils.getGson();
		Log.i(TAG, "ProcessStatisticsProbe DATA: " + gson.toJson(data));
	}
}
