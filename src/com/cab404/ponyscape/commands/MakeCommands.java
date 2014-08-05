package com.cab404.ponyscape.commands;

import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.ponyscape.android.MainActivity;
import com.cab404.ponyscape.bus.events.Commands;
import com.cab404.ponyscape.bus.events.Shortcuts;
import com.cab404.ponyscape.utils.Static;

import java.util.List;

/**
 * @author cab404
 */
@CommandClass(prefix = "make")
public class MakeCommands {

	@SuppressWarnings("unchecked")
	@Command(command = "alias", params = {Str.class, Str.class})
	public void shortcut(String name, String command) {
		MainActivity.LaunchShortcut shortcut = new MainActivity.LaunchShortcut(name, command);
		((List<MainActivity.LaunchShortcut>) Static.settings.get("main.shortcuts")).add(shortcut);
		Static.bus.send(new Shortcuts.Update());
		Static.bus.send(new Commands.Clear());
		Static.bus.send(new Commands.Finished());

	}

}