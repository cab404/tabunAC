package com.cab404.ponyscape.commands;

import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.ponyscape.utils.Simple;

/**
 * @author cab404
 */
@CommandClass(prefix = "load")
public class LoadCommands {

	@Command(command = "page", params = Str.class)
	public void post(String post) {
		Simple.redirect("page load " + post);
	}

}
