package com.cab404.ponyscape.android;

import android.app.IntentService;
import android.content.Intent;

/**
 * @author cab404
 */
public class IntentResolver extends IntentService {

	public IntentResolver() {
		this("PonyscapeIntentResolver");
	}

	public IntentResolver(String name) {
		super(name);
	}

	@Override protected void onHandleIntent(Intent intent) {

	}
}
