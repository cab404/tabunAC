package com.cab404.ponyscape.android;

import android.app.Activity;
import android.os.Bundle;
import com.cab404.ponyscape.utils.Static;

/**
 * @author cab404
 */
public class AbstractActivity extends Activity {

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Static.bus.register(this);
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		Static.bus.unregister(this);
	}

}
