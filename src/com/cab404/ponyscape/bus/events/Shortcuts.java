package com.cab404.ponyscape.bus.events;

import com.cab404.moonlight.util.SU;

/**
 * @author cab404
 */
public class Shortcuts {

	public static class Update { }

	/**
	 * Алиас.
	 */
	public static class LaunchShortcut {
		public final String name;
		public final String command;

		public LaunchShortcut(String serialized) {
			String[] data = SU.splitToArray(serialized, '/');

			name = SU.drl(data[0]);
			command = SU.drl(data[1]);
		}

		public String toString() {
			return SU.rl(name) + "/" + SU.rl(command);
		}

		public LaunchShortcut(String name, String command) {
			this.name = name;
			this.command = command;
		}

	}
}
