package com.cab404.ponyscape.events;

/**
 * @author cab404
 */
public class Commands {

	public static class Run {
		public final String command;
		public Run(String command) {
			this.command = command;
		}
	}

	public static class Finished { }

	/**
	 * Очищает бар с командой.
	 */
	public static class Clear { }

}
