package com.cab404.sjbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * @author cab404
 */
public class PendingMethod {
	public final Method method;
	public final Object holder;
	public final Class<? extends Executor> starter;

	public PendingMethod(Method method, Object holder) {
		this.method = method;
		this.holder = holder;
		starter = method.getAnnotation(Bus.Handler.class).executor();
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
