package com.cab404.jconsol.converters;


import com.cab404.jconsol.ParameterConverter;

/**
 * Integer converter
 *
 * @author cab404
 */
public class Int implements ParameterConverter<Integer> {

	@Override public Integer convert(String data) {
		return Integer.valueOf(data.startsWith("+") ? data.substring(1) : data);
	}

	@Override public boolean isInstance(String data) {
		try {
			//noinspection ResultOfMethodCallIgnored
			Integer.parseInt(data.startsWith("+") ? data.substring(1) : data);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
