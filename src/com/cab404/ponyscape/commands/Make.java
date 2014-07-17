package com.cab404.ponyscape.commands;

import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.ponyscape.android.MainActivity;
import com.cab404.ponyscape.events.Commands;
import com.cab404.ponyscape.events.Shortcuts;
import com.cab404.ponyscape.utils.Bus;
import com.cab404.ponyscape.utils.Static;

import java.util.List;

/**
 * @author cab404
 */
@CommandClass(prefix = "make")
public class Make {

	@SuppressWarnings("unchecked")
	@Command(command = "shortcut", params = {Str.class, Str.class})
	public void shortcut(String name, String command) {
		MainActivity.LaunchShortcut shortcut = new MainActivity.LaunchShortcut(name, command);
		((List<MainActivity.LaunchShortcut>) Static.settings.get("main.shortcuts")).add(shortcut);
		Bus.send(new Shortcuts.Update());
		Bus.send(new Commands.Clear());
		Bus.send(new Commands.Finished());

	}

}