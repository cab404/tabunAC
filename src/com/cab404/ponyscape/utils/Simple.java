package com.cab404.ponyscape.utils;

import android.widget.Toast;
import com.cab404.moonlight.util.SU;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author cab404
 */
public class Simple {

	public static void msg(CharSequence text) {
		Toast.makeText(Static.app_context, text, Toast.LENGTH_SHORT).show();
	}

	public static URI parse(String url)
	throws URISyntaxException {
		List<String> split = SU.split(url, "/");

		for (int i = 1; i < split.size(); i++) {
			split.set(i, SU.rl(split.get(i)));
		}

		return new URI(SU.join(split, "/"));
	}

}
