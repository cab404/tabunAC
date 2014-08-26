package com.cab404.ponyscape.android;

import android.app.Application;
import android.os.Handler;
import android.util.Log;
import com.cab404.jconsol.CommandManager;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.ponyscape.commands.*;
import com.cab404.ponyscape.utils.Settings;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.images.Images;
import com.cab404.sjbus.Bus;

import java.io.File;
import java.util.ArrayList;

/**
 * @author cab404
 */
public class App extends Application {
	@Override public void onCreate() {
		super.onCreate();

		/* Готовим место для картинок */
		File image_storage = new File(getFilesDir(), "img");
		if (!image_storage.exists() && !image_storage.mkdirs())
			Log.w("Bootstrap", "Images won't be avalible.");

        /* Инициализируем статику. */
		Static.bus = new Bus();
		Static.cm = new CommandManager();
		Static.user = new TabunAccessProfile();
		Static.img = new Images(this, image_storage);
		Static.app_context = getApplicationContext();
		Static.handler = new Handler(getMainLooper());
		Static.history = new ArrayList<>();
		Static.cfg = Settings.load(this, "settings.bin");

		Static.img.reconfigure();

        /* Регестрируем обработчики команд */
		Static.cm.register(CoreCommands.class);
		Static.cm.register(PostCommands.class);
		Static.cm.register(PageCommands.class);
		Static.cm.register(VoteCommands.class);
		Static.cm.register(MakeCommands.class);
		Static.cm.register(UserCommands.class);

	}

}
