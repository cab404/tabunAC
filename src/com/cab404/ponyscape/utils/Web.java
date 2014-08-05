package com.cab404.ponyscape.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author cab404
 */
public class Web {

	public static class NetworkNotFound extends RuntimeException {}

	public static void checkNetworkConnection() {
		ConnectivityManager net =
				(ConnectivityManager) Static.app_context.getSystemService(Context.CONNECTIVITY_SERVICE);

		for (NetworkInfo info : net.getAllNetworkInfo())
			if (info.isAvailable() && info.isConnected())
				return;

		throw new NetworkNotFound();
	}

}
