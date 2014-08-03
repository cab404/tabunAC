package com.cab404.jconsol;


import com.cab404.jconsol.annotations.Command;

import java.lang.reflect.Method;

/**
 * @author cab404
 */
public class CommandHolder {

    public final Method method;
    public final String prefix;
    public final Object object;
    public final Command annnotation;

    public CommandHolder(Method method, Object object, String namespace, Command annnotation) {
        this.method = method;
        this.object = object;
        this.prefix = namespace;
        this.annnotation = annnotation;
    }
}
