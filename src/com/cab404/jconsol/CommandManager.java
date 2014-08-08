package com.cab404.jconsol;


import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.util.ArrayMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * @author cab404
 */
public class CommandManager {

	private HashMap<String, ArrayMap<String, CommandHolder>> data;

	public CommandManager() {
		data = new HashMap<>();
	}

	public void register(Class<?> clazz) {
		try {
			CommandClass annotation = clazz.getAnnotation(CommandClass.class);

			if (annotation != null) {

				// Ensures what we have registered storage cell.
				ArrayMap<String, CommandHolder> clazz_data;
				if (data.containsKey(annotation.prefix())) {
					clazz_data = data.get(annotation.prefix());
				} else {
					clazz_data = new ArrayMap<>();
					data.put(annotation.prefix(), clazz_data);
				}

				// Creating new instance for running commands.
				Object obj = clazz.getConstructor().newInstance();

				for (Method m : clazz.getMethods()) {
					Command command = m.getAnnotation(Command.class);

					if (command != null) {

						CommandHolder holder = new CommandHolder(m, obj, annotation.prefix(), command);

						clazz_data.add(command.command(), holder);

					}
				}
			}

		} catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean isEscaped(int char_index, CharSequence seq) {
		char escape = '\\';
		if (char_index == 0)
			return false;
		else if (seq.charAt(char_index - 1) == escape)
			return !isEscaped(char_index - 1, seq);
		else
			return false;
	}

	public static String escapeString(String str){
		return str
				.replace("\"", "\\\"")
				.replace(";", "\\;")
				.replace(" ", "\\ ");
	}

	/**
	 * Splits lines of script via ';'.
	 */
	public static List<String> splitCommandLines(String toSplit) {
		Collection<Integer> splitPoints = new ArrayList<>();
		for (int i = 0; i < toSplit.length(); i++)
			if (toSplit.charAt(i) == ';')
				if (!isEscaped(i, toSplit))
					splitPoints.add(i);

		List<String> split = new ArrayList<>();
		int last = 0;
		for (int splitPoint : splitPoints) {
			split.add(toSplit.substring(last, splitPoint));
			last = splitPoint + 1;
		}
		split.add(toSplit.substring(last));

		return split;
	}

	/**
	 * Splits command to String array.
	 */
	private static List<String> splitCommand(String toSplit) {
		int index = 0, li = 0;

		int count = 0;

		for (int i = 0; i < toSplit.length(); i++)
			if (toSplit.charAt(i) == '\"' && !isEscaped(i, toSplit))
				count++;

		if (count % 2 != 0)
			throw new RuntimeException("Non-enclosed quotes found!");


        /* Specifies whether we currently parsing quote-enclosed statement */
		boolean apos = false;
		ArrayList<String> data = new ArrayList<>();

		while (index < toSplit.length()) {
			String toAdd = null;
			switch (toSplit.charAt(index)) {
				case ' ':
					if (isEscaped(index, toSplit)) break;
					if (!apos) {
						toAdd = toSplit.substring(li, index).trim();
						if (toAdd.isEmpty()) toAdd = null;
						li = index + 1;
					} else {
						// Manually adding escape character.
						toSplit = toSplit.substring(0, index) + "\\" + toSplit.substring(index);
					}
					break;
				case '\"':
					if (isEscaped(index, toSplit)) break;
					if (apos) {
						toAdd = toSplit.substring(li, index);
						apos = false;
					} else {
						apos = true;
					}
					li = index + 1;
					break;
			}

			if (toAdd != null) {
				data.add(toAdd);
			}
			index++;
		}


		if (index != li) {
			String toAdd = toSplit.substring(li, index);
			data.add(toAdd);
		}

		/* Removing escapes. */
		for (int i = 0; i < data.size(); i++) {
			String entry = data.get(i);

			for (int j = 0; j < entry.length(); j++)
				switch (entry.charAt(j)) {
					case ' ':
					case '\"':
						entry = entry.substring(0, j - 1) + entry.substring(j);
				}

			data.set(i, entry);
		}

		return data;
	}

	private HashMap<Class<? extends ParameterConverter>, ParameterConverter> converters = new HashMap<>();
	private ParameterConverter getConverter(Class<? extends ParameterConverter> clazz) {
		try {
			if (!converters.containsKey(clazz))
				converters.put(clazz, clazz.newInstance());
			return converters.get(clazz);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void run(String str) {

		List<String> parts = splitCommand(str);

		if (parts.isEmpty()) return;

		ArrayMap<String, CommandHolder> commands;
		if (parts.size() > 1 && data.containsKey(parts.get(0))) {
			commands = data.get(parts.get(0));
		} else if (data.containsKey("")) {
			parts.add(0, "");
			commands = data.get("");
		} else
			throw new CommandNotFoundException();


        /* Searching for command */
		try {

			Object[] parameters = new Object[parts.size() - 2];

			search:
			for (CommandHolder holder : commands.getValues(parts.get(1))) {
				Class<? extends ParameterConverter>[] params = holder.annnotation.params();
				if (params.length == parts.size() - 2) {

					for (int i = 0; i < params.length; i++) {
						ParameterConverter conv = getConverter(params[i]);

						if (conv.isInstance(parts.get(i + 2)))
							parameters[i] = conv.convert(parts.get(i + 2));
						else continue search;

					}

					holder.method.invoke(holder.object, parameters);
					return;
				}

			}

		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		throw new CommandNotFoundException();

	}


	public List<CommandHolder> registered() {
		ArrayList<CommandHolder> out = new ArrayList<>();
		for (ArrayMap<String, CommandHolder> am : data.values()) {
			for (CommandHolder holder : am.values())
				out.add(holder);
		}
		return out;
	}

}
