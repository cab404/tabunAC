package com.cab404.ponyscape.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.widget.Toast;
import com.cab404.libtabun.data.Letter;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author cab404
 */
public class Simple {

	public static void msg(CharSequence text) {
		Toast.makeText(Static.ctx, text, Toast.LENGTH_SHORT).show();
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
			return (SU.join(letter.recipients, ", ").toString());

	}


	public static String md5(String str) {
		try {
			return Base64.encodeToString(MessageDigest.getInstance("MD5").digest(String.valueOf(str).getBytes(Charset.forName("UTF-8"))), Base64.URL_SAFE);
		} catch (NoSuchAlgorithmException e) {
			return "NO-MD-CAN-BE-CALCULATED";
		}
	}

	public static void checkNetworkConnection() {
		ConnectivityManager net =
				(ConnectivityManager) Static.ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

		for (NetworkInfo info : net.getAllNetworkInfo())
			if (info.isAvailable() && info.isConnected())
				return;

		throw new NetworkNotFound();
	}

	public static void redirect(String to) {
		Static.bus.send(new E.Commands.Finished());
		Static.bus.send(new E.Commands.Run(to));
	}


	public static class NetworkNotFound extends RuntimeException { }
}
