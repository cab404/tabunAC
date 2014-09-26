package com.cab404.ponyscape.utils.state;

import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.utils.Static;
import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cab404
 */
public class AliasUtils {

	public static List<Alias> getAliases() {
		ArrayList<Alias> aliases = new ArrayList<>();

		JSONArray shortcuts = (JSONArray) Static.obscure.get("main.shortcuts");
		if (shortcuts == null)
			shortcuts = new JSONArray();

		for (Object string_actulally : shortcuts)
			aliases.add(new Alias(string_actulally.toString()));


		Static.obscure.put("main.shortcuts", shortcuts);
		Static.cfg.save();

		return aliases;

	}

	@SuppressWarnings("unchecked")
	public static void setAliases(Iterable<Alias> aliases) {
		JSONArray shortcuts = new JSONArray();

		for (Alias alias : aliases)
			shortcuts.add(alias.toString());


		Static.obscure.put("main.shortcuts", shortcuts);
		Static.obscure.save();
	}

	/**
	 * Алиас.
	 */
	public static class Alias {
		public final String name;
		public final String command;

		public Alias(String serialized) {
			String[] data = SU.splitToArray(serialized, '/');

			name = SU.drl(data[0]);
			command = SU.drl(data[1]);
		}

		public String toString() {
			return SU.rl(name) + "/" + SU.rl(command);
		}

		public Alias(String name, String command) {
			this.name = name;
			this.command = command;
		}

	}
}
