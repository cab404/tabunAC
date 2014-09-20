package com.cab404.ponyscape.commands;

import android.content.Intent;
import com.cab404.acli.PartActivity;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.state.AliasUtils;
import org.json.simple.JSONArray;

/**
 * @author cab404
 */
@CommandClass(prefix = "make")
public class MakeCommands {

	@SuppressWarnings("unchecked")
	@Command(command = "alias", params = {Str.class, Str.class})
	public void shortcut(String name, String command) {
		AliasUtils.Alias shortcut = new AliasUtils.Alias(name, command);
		((JSONArray) Static.cfg.get("main.shortcuts")).add(shortcut.toString());
		Static.bus.send(new E.Aliases.Update());
		Static.bus.send(new E.Commands.Clear());
		Static.bus.send(new E.Commands.Finished());

	}

	@SuppressWarnings("unchecked")
	@Command(command = "editor")
	public void editor() {
		Static.bus.send(new E.Android.StartActivity(new Intent(Static.app_context, PartActivity.class)));
	}

}