package com.cab404.ponyscape.bus.events;

import android.content.Intent;

/**
 * @author cab404
 */
public class Android {

	public static class RootSizeChanged { }

	/**
	 * Запускает задание из текущей Activity
	 */
	public static class StartActivityForResult {
		public static interface ResultHandler {
			public void handle(int resultCode, Intent intent);
			public void error(Throwable e);
		}

		public final Intent intent;
		public final ResultHandler handler;
		public StartActivityForResult(Intent intent, ResultHandler handler) {
			this.intent = intent;
			this.handler = handler;
		}
	}

	public static class StartActivity {
		public final Intent activity;
		public StartActivity(Intent activity) {this.activity = activity;}
	}

}
