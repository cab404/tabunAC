package com.cab404.ponyscape.commands;

import android.content.Intent;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.acli.PartActivity;
import com.cab404.ponyscape.android.MainActivity;
import com.cab404.ponyscape.bus.events.Android;
import com.cab404.ponyscape.bus.events.Commands;
import com.cab404.ponyscape.bus.events.Shortcuts;
import com.cab404.ponyscape.utils.Static;
import org.json.simple.JSONArray;

/**
 * @author cab404
 */
@CommandClass(prefix = "make")
public class MakeCommands {

	@SuppressWarnings("unchecked")
	@Command(command = "alias", params = {Str.class, Str.class})
	public void shortcut(String name, String command) {
		MainActivity.LaunchShortcut shortcut = new MainActivity.LaunchShortcut(name, command);
		((JSONArray) Static.cfg.get("main.shortcuts")).add(shortcut.toString());
		Static.bus.send(new Shortcuts.Update());
		Static.bus.send(new Commands.Clear());
		Static.bus.send(new Commands.Finished());

	}

	@SuppressWarnings("unchecked")
	@Command(command = "editor")
	public void editor() {
		Static.bus.send(new Android.StartActivity(new Intent(Static.app_context, PartActivity.class)));
	}

}