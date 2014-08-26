package com.cab404.ponyscape.utils;

import android.widget.Toast;

/**
 * @author cab404
 */
public class Simple {

	public static void msg(CharSequence text) {
		Toast.makeText(Static.app_context, text, Toast.LENGTH_SHORT).show();
	}

	public static String parse(String url) {
		return url
				.replace("[", "%5B")
				.replace(":", "%3A");
	}

}
