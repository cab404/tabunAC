package com.cab404.ponyscape.utils;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple settings saver
 *
 * @author cab404
 */
public class Settings {

	private Map<String, Object> data = new HashMap<>();
	private File file;

	public Settings(Context context, String filename) {
		file = new File(context.getFilesDir(), filename);
	}

	private final Object
			readLock = new Object(),
			writeLock = new Object();

	public void put(String key, Object value) {
		synchronized (writeLock) {
			synchronized (readLock) {
				data.put(key, value);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		synchronized (readLock) {
			return (T) data.get(key);
		}
	}
	@SuppressWarnings("unchecked")
	public <T> T remove(String key) {
		synchronized (writeLock) {
			synchronized (readLock) {
				return (T) data.remove(key);
			}
		}
	}


	public void save() {
		Log.v("Settings", "Saving...");
		try {
			ObjectOutput out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			out.writeObject(data);
			out.close();
			Log.v("Settings", "Saved!");
		} catch (IOException e) {
			throw new RuntimeException("Save error!", e);
		}
	}

	@SuppressWarnings("unchecked")
	public void load() {
		Log.v("Settings", "Loading...");
		try {
			ObjectInput in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
			data = (Map<String, Object>) in.readObject();
			in.close();
			Log.v("Settings", "Loaded! " + data);
		} catch (FileNotFoundException e) {
			Log.v("Settings", "Settings file was not found, creating.");
			save();
			data = new HashMap<>();
		} catch (ClassCastException | IOException | ClassNotFoundException e) {
			Log.e("Settings", "Load error!", e);
			save();
			data = new HashMap<>();
		}
	}

}
