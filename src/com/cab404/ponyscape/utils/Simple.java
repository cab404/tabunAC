package com.cab404.ponyscape.utils;

import android.content.Context;
import android.widget.Toast;
import com.cab404.libtabun.data.Letter;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;

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

	public static String buildRecipients(Context context, Letter letter) {
		int cut = 2;

		if (letter.recipients.size() > cut + 1) {
			int i = letter.recipients.size() - cut;
			return (
					SU.join(letter.recipients.subList(0, cut), ", ")
							+ String.format(context.getString(R.string.mail_recipients), i)
			);
		} else
			return (SU.join(letter.recipients, ", "));

	}


}
