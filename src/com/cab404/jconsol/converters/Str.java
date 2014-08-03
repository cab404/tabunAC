package com.cab404.jconsol.converters;


import com.cab404.jconsol.ParameterConverter;

/**
 * String converter
 *
 * @author cab404
 */
public class Str implements ParameterConverter<String> {

    @Override public String convert(String data) {
        return data;
    }

    @Override public boolean isInstance(String data) {
        return true;
    }

}
