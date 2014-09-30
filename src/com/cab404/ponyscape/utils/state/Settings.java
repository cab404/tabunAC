package com.cab404.ponyscape.utils.state;

import android.content.Context;
import android.util.Log;
import com.cab404.ponyscape.utils.Static;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

/**
 * Simple settings saver
 *
 * @author cab404
 */
public class Settings {

	public JSONObject data;
	private File file;

	private Settings(JSONObject data, File file) {
		this.data = data;
		this.file = file;
	}

	public Settings(Context context, String filename) {
		this(new JSONObject(), new File(context.getFilesDir(), filename));
	}

	@SuppressWarnings("unchecked")
	/**
	 *  Если есть, то достаёт, если нет, то возвращает, что дают, и кладёт это в значение.
	 */
	public <T> T ensure(String key, T def_value) {
		Object curr = Static.cfg.get(key);

		if (curr == null) {
			put(key, def_value);
			save();
			return def_value;
		} else {
			if (Integer.class.isAssignableFrom(def_value.getClass()))
				curr = Integer.parseInt(curr.toString());
			return (T) curr;
		}
	}

	/**
	 * data.get(key)
	 */
	public Object get(String key) {
		return data.get(key);
	}

	/**
	 * data.put with suppression of unchecked.
	 */
	@SuppressWarnings("unchecked")
	public void put(String key, Object object) {
		data.put(key, object);
	}

	private final Object
			readLock = new Object(),
			writeLock = new Object();
	public void save() {
		Log.v("Settings", "Saving...");
		try {

			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			data.writeJSONString(writer);
			writer.close();

			Log.v("Settings", "Saved!");
		} catch (IOException e) {
			throw new RuntimeException("Save error!", e);
		}
	}

	@SuppressWarnings("unchecked")
	public static Settings load(Context context, String filename) {
		File file = new File(context.getFilesDir(), filename);
		Settings data;

		try {
			Log.v("Settings", "Loading...");

			BufferedReader reader = new BufferedReader(new FileReader(file));

			data = new Settings((JSONObject) new JSONParser().parse(reader), file);

			reader.close();
			Log.v("Settings", "Loaded! " + data);
		} catch (FileNotFoundException e) {
			Log.v("Settings", "Settings file was not found, creating.");
			data = new Settings(context, filename);
			data.save();
		} catch (ParseException | IOException e) {
			Log.e("Settings", "Load error!", e);
			data = new Settings(context, filename);
			data.save();
		}

		return data;
	}

}
