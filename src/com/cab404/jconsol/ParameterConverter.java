package com.cab404.jconsol;

/**
 * @author cab404
 */
public interface ParameterConverter<T> {
    /**
     * Converts string to an object.
     */
    public T convert(String data);
    /**
     * Checks whether string can be converted.
     */
    public boolean isInstance(String data);
}
