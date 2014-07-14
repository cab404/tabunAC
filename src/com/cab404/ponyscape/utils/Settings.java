package com.cab404.ponyscape.utils;

import android.content.Context;

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

	public Map<String, Object> getData() {
		return data;
	}

	public Settings(Context context, String filename) {
		file = new File(context.getFilesDir(), filename);
	}

	public void save() {
		try {
			ObjectOutput out = null;
			try {
				out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(data);
			} finally {
				if (out != null)
					out.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public void load() {
		try {
			ObjectInput in = new ObjectInputStream(new FileInputStream(file));
			try {
				data = (Map<String, Object>) in.readObject();
			} catch (ClassCastException e) {
				data = new HashMap<>();
			}
			in.close();

		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
