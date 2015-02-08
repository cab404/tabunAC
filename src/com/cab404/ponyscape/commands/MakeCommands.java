package com.cab404.ponyscape.commands;

import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.state.AliasUtils;
import org.json.simple.JSONArray;

import java.util.List;

/**
 * @author cab404
 */
@CommandClass(prefix = "make")
public class MakeCommands {

	@SuppressWarnings("unchecked")
	@Command(command = "alias", params = {Str.class, Str.class})
	public void shortcut(String name, String command) {

        List<AliasUtils.Alias> aliases = AliasUtils.getAliases();
        aliases.add(new AliasUtils.Alias(name, command));
        AliasUtils.setAliases(aliases);

        Static.bus.send(new E.Aliases.Update());
		Static.bus.send(new E.Commands.Clear());
		Static.bus.send(new E.Commands.Finished());
	}


}