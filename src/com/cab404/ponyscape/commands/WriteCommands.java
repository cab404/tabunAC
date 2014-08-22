package com.cab404.ponyscape.commands;

import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Int;

/**
 * @author cab404
 */
@CommandClass(prefix = "write")
public class WriteCommands {

	@Command(command = "comment", params = {Int.class, Int.class})
	public void comment(int topic, int response) {

	}

}
