package com.cab404.ponyscape.utils;

import android.util.Log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple bus
 *
 * @author cab404
 */
public class Bus {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Handler { }

    public static class PendingMethod {
        public final Method method;
        public final Object holder;

        public PendingMethod(Method method, Object holder) {
            this.method = method;
            this.holder = holder;
        }

        public boolean canBeInvokedWith(Object... parameters) {

            if (!(parameters.length == method.getParameterTypes().length || method.isVarArgs()))
                return false;

            Class[] m_par = method.getParameterTypes();

            for (int i = 0; i != parameters.length; i++) {
                Class<?> p_type = parameters[i].getClass();
                Class<?> m_type = m_par[i >= m_par.length ? m_par.length - 1 : i];

                if (!m_type.isAssignableFrom(p_type)) return false;
            }

            return true;
        }

        public void invoke(Object... parameters) {
            try {
                method.invoke(holder, parameters);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static List<PendingMethod> handlers = new ArrayList<>();

    public static void register(Object obj) {
        unregister(obj);
        Log.v("Bus", "Handling object " + obj);
        for (Method method : obj.getClass().getMethods())
            if (method.getAnnotation(Handler.class) != null)
                handlers.add(new PendingMethod(method, obj));
    }


    public static void send(Object event) {
        Log.v("Bus", "Handling event " + event);
        for (PendingMethod method : handlers) {
            Log.v("Bus", "Checking " + method.method.getName());

            if (method.canBeInvokedWith(event)) {
                Log.v("Bus", "Match, invoking " + method.method.getName());
                method.invoke(event);
            }
        }
    }

    public static void unregister(Object obj) {
        for (int i = 0; i < handlers.size(); i++)
            if (handlers.get(i).holder.equals(obj))
                handlers.remove(i);
            else
                i++;
    }

}
