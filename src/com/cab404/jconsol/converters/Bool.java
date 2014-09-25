package com.cab404.jconsol.converters;


import com.cab404.jconsol.ParameterConverter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Boolean converter
 *
 * @author cab404
 */
public class Bool implements ParameterConverter<Boolean> {

	private static final List
			YES = Collections.unmodifiableList(Arrays.asList("y", "yes", "true", "+")),
			NO = Collections.unmodifiableList(Arrays.asList("n", "no", "false", "-"));

	@Override public Boolean convert(String data) {
		return YES.contains(data.trim().toLowerCase());
	}

	@Override public boolean isInstance(String data) {
		return YES.contains(data.trim().toLowerCase()) || NO.contains(data.trim().toLowerCase());
	}

}
