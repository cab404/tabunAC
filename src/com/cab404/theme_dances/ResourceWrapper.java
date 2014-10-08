package com.cab404.theme_dances;

import android.content.res.Resources;

import java.util.Map;

/**
 * Let us try to not destroy universe, we're just redirecting color flow.
 *
 * @author cab404
 */
public class ResourceWrapper extends Resources {
	private Map<Integer, Integer> theme;

	public ResourceWrapper(Resources res, Map<Integer, Integer> theme) {
		super(res.getAssets(), res.getDisplayMetrics(), res.getConfiguration());
		this.theme = theme;
	}

	@Override public int getColor(int id)
	throws NotFoundException {
		if (theme.containsKey(id))
			return theme.get(id);
		return super.getColor(id);
	}
}
