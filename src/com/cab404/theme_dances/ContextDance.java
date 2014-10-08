package com.cab404.theme_dances;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.view.LayoutInflater;

import java.util.Map;

/**
 * The waltz goes on.
 *
 * @author cab404
 */
public class ContextDance extends ContextWrapper {

	private Resources resources;
	private ResourceWrapper res;
	private LayoutInflater inflater;

	public ContextDance(Context base, Map<Integer, Integer> theme, Resources resources) {
		super(base);
		res = new ResourceWrapper(resources, theme);
	}

	@Override public ResourceWrapper getResources() {
		return res;
	}

	@Override
	public Object getSystemService(String name) {
		if (LAYOUT_INFLATER_SERVICE.equals(name)) {
			if (inflater == null) inflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
			return inflater;
		}
		return super.getSystemService(name);
	}

}
