package com.cab404.ponyscape.android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.cab404.ponyscape.utils.Static;

/**
 * @author cab404
 */
public class AbstractActivity extends Activity {

	static final boolean THEMES_ENABLED = Static.cfg.ensure("main.themes_enabled", false);

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Static.bus.register(this);
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		Static.bus.unregister(this);
	}

	@Override protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(
				THEMES_ENABLED ?
						Static.theme.getContext(newBase) : newBase
		);
	}

}
