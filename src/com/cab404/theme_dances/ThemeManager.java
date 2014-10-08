package com.cab404.theme_dances;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import org.json.simple.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the theme
 *
 * @author cab404
 */
public class ThemeManager {

	private final Map<String, Integer> occlusion_list;
	private final Resources base;
	private final Map<Integer, Integer> resolved;

	/**
	 * @param occlusion_list Resolving names to their real places.
	 */
	public ThemeManager(Map<String, Integer> occlusion_list, Resources base) {
		this.resolved = new ConcurrentHashMap<>();
		this.occlusion_list = occlusion_list;
		this.base = base;
	}

	public void setTheme(JSONObject theme) {
		resolved.clear();
		if (theme == null) return;

		for (Object e : theme.keySet())
			if (occlusion_list.containsKey(e))
				try {
					Integer key = occlusion_list.get(e);
					String color = (String) theme.get(e);

					Log.v("ThemeOrchestra", key + ": " + color);
					resolved.put(
							key,
							Color.parseColor(color)
					);
				} catch (Exception ex) {
					Log.w("ThemeOrchestra", "An error occurred while we were trying to parse key " + e, ex);
				}

	}

	public ContextDance getContext(Context underlying) {
		return new ContextDance(underlying, resolved, base);
	}

}
