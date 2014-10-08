package com.cab404.ponyscape.android;

import android.app.Application;
import android.os.Handler;
import android.util.Log;
import com.cab404.acli.PartActivity;
import com.cab404.jconsol.CommandManager;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.commands.*;
import com.cab404.ponyscape.utils.Pools;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.images.Images;
import com.cab404.ponyscape.utils.state.Settings;
import com.cab404.sjbus.Bus;
import com.cab404.theme_dances.ThemeManager;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cab404
 */
public class App extends Application {

	@Override public void onCreate() {
		super.onCreate();

		/* Готовим место для картинок */
		File image_storage = new File(getCacheDir(), "img");
		if (!image_storage.exists() && !image_storage.mkdirs())
			Log.w("Bootstrap", "Images won't be avalible.");

		Static.dp = getBaseContext().getResources().getDisplayMetrics().density;

        /* Инициализируем статику. */
		Static.bus = new Bus();

		Static.cm = new CommandManager();
		Static.ctx = getApplicationContext();
		Static.user = new TabunAccessProfile();
		Static.pools = new Pools();
		Static.handler = new Handler(getMainLooper());

		Static.cfg = Settings.load(this, "settings.bin");
		Static.obscure = Settings.load(this, "obscure.cfg");

		Static.img = new Images(this, image_storage);
		Static.theme = new ThemeManager(generateColorResolver(), getResources());
		Static.history = new ArrayList<>();

		Static.theme.setTheme(new JSONObject());
		Static.img.reconfigure();
		PartActivity.setup();

	    /* Регестрируем обработчики команд */
		Static.cm.register(CoreCommands.class);
		Static.cm.register(PostCommands.class);
		Static.cm.register(PageCommands.class);
		Static.cm.register(VoteCommands.class);
		Static.cm.register(MakeCommands.class);
		Static.cm.register(UserCommands.class);
		Static.cm.register(TalkCommands.class);
		Static.cm.register(LikeCommands.class);
		Static.cm.register(LoadCommands.class);
		Static.cm.register(SaveCommands.class);
		Static.cm.register(ArchCommands.class);
	}

	private Map<String, Integer> generateColorResolver() {
		HashMap<String, Integer> resolver = new HashMap<>();

		resolver.put("comment_ladder_gradient_start", R.color.comment_ladder_gradient_start);
		resolver.put("comment_ladder_gradient_end", R.color.comment_ladder_gradient_end);
		resolver.put("font_color_command_line", R.color.font_color_command_line);
		resolver.put("font_color_shortcuts", R.color.font_color_shortcuts);
		resolver.put("font_color_blue", R.color.font_color_blue);
		resolver.put("font_color_green", R.color.font_color_green);
		resolver.put("font_color_link", R.color.font_color_link);
		resolver.put("bg_item_shadow", R.color.bg_item_shadow);
		resolver.put("font_color_red", R.color.font_color_red);
		resolver.put("bg_item_label", R.color.bg_item_label);
		resolver.put("bg_shortcuts", R.color.bg_shortcuts);
		resolver.put("bg_item_new", R.color.bg_item_new);
		resolver.put("bg_item_fav", R.color.bg_item_fav);
		resolver.put("code_color", R.color.code_color);
		resolver.put("font_color", R.color.font_color);
		resolver.put("spoiler_bg", R.color.spoiler_bg);
		resolver.put("cut_border", R.color.cut_border);
		resolver.put("code_bg", R.color.code_bg);
		resolver.put("bg_main", R.color.bg_main);
		resolver.put("bg_tint", R.color.bg_tint);
		resolver.put("bg_item", R.color.bg_item);
		resolver.put("cut_bg", R.color.cut_bg);

		Log.v("Resolver", resolver.toString());
		return resolver;
	}

}
