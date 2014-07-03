package com.cab404.ponyscape.utils;

import android.content.Context;
import android.os.Handler;
import com.cab404.acli.base.ACLIList;
import com.cab404.jconsol.CommandManager;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.libtabun.util.TabunAccessProfile;

/**
 * @author cab404
 */
public class Static {
    public static ACLIList list = null;
    public static CommandManager cm = null;

    public static TabunAccessProfile user = null;
    public static TabunPage last_page = null;

    public static Context app_context = null;
    public static Handler handler = null;
}
