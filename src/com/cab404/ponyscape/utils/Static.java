package com.cab404.ponyscape.utils;

import android.content.Context;
import android.os.Handler;
import com.cab404.jconsol.CommandManager;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.ponyscape.utils.images.Images;
import com.cab404.ponyscape.utils.state.Settings;
import com.cab404.sjbus.Bus;

import java.util.ArrayList;

/**
 * Static
 *
 * @author cab404
 */
public class Static {

	public static ArrayList<String> history = null;

	public static CommandManager cm = null;
	public static Settings cfg = null;
	public static Bus bus = null;


	public static TabunAccessProfile user = null;
	public static TabunPage last_page = null;

	public static Context app_context = null;
	public static Handler handler = null;
	public static Pools pools = null;

	public static Images img = null;

}
