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

        /* Инициализируем статику. */
		Static.bus = new Bus();
		Static.cm = new CommandManager();
		Static.user = new TabunAccessProfile();
		Static.img = new Images(this, image_storage);
		Static.ctx = getApplicationContext();
		Static.handler = new Handler(getMainLooper());
		Static.history = new ArrayList<>();
		Static.cfg = Settings.load(this, "settings.bin");
		Static.obscure = Settings.load(this, "obscure.cfg");
		Static.pools = new Pools();


		JSONObject object = new JSONObject();
//		object.put("font_color_command_line", "#FF0000");
//		object.put("bg_main", "#FF0000");
//		object.put("bg_tint", "#FF0000");
//		object.put("bg_item", "#FF0000");
//		object.put("bg_item_shadow", "#FF0000");
//		object.put("bg_item_new", "#FF0000");
//		object.put("bg_item_fav", "#FF0000");
//		object.put("bg_item_label", "#FF0000");
//		object.put("bg_shortcuts", "#FF0000");
//		object.put("font_color_shortcuts", "#FF0000");
//		object.put("font_color", "#FF0000");
//		object.put("font_color_link", "#FF0000");
//		object.put("font_color_red", "#FF0000");
//		object.put("font_color_blue", "#FF0000");
//		object.put("font_color_green", "#FF0000");
//		object.put("comment_ladder_gradient_start", "#FF0000");
//		object.put("comment_ladder_gradient_end", "#FF0000");
//		object.put("spoiler_bg", "#FF0000");
//		object.put("cut_border", "#FF0000");
//		object.put("cut_bg", "#FF0000");
//		object.put("code_bg", "#FF0000");
//		object.put("code_color", "#FF0000");

		Static.theme = new ThemeManager(generateColorResolver(), getResources());

		Static.theme.setTheme(object);
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
