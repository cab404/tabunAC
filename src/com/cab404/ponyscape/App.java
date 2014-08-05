package com.cab404.ponyscape;

import android.app.Application;
import android.os.Handler;
import com.cab404.jconsol.CommandManager;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.ponyscape.commands.*;
import com.cab404.ponyscape.utils.Settings;
import com.cab404.ponyscape.utils.Static;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */
public class App extends Application {
	@Override public void onCreate() {
		super.onCreate();

        /* Инициализируем статику. */
		Static.bus = new Bus();
		Static.cm = new CommandManager();
		Static.user = new TabunAccessProfile();
		Static.app_context = getApplicationContext();
		Static.handler = new Handler(getMainLooper());
		Static.settings = new Settings(this, "settings.bin");
		Static.settings.load();

        /* Регестрируем обработчики команд */
		Static.cm.register(CoreCommands.class);
		Static.cm.register(PostCommands.class);
		Static.cm.register(PageCommands.class);
		Static.cm.register(VoteCommands.class);
		Static.cm.register(MakeCommands.class);



	}

	@Override public void onTerminate() {
		super.onTerminate();
		Static.bus.unregister(this);
		Static.settings.save();
	}

}
