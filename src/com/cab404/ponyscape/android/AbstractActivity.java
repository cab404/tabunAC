package com.cab404.ponyscape.android;

import android.app.Activity;
import android.os.Bundle;
import com.cab404.ponyscape.utils.Bus;

/**
 * @author cab404
 */
public class AbstractActivity extends Activity {

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bus.register(this);
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		Bus.unregister(this);
	}

}
