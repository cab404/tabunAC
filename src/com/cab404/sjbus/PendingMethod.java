package com.cab404.sjbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * @author cab404
 */
public class PendingMethod {
	/**
	 * Method to execute.
	 */
	private final Method method;
	/**
	 * Object, with which method will be executed.
	 */
	private final Object holder;
	public final Class<? extends Executor> starter;
	public final String label;
	private final Class<?>[] m_par;

	/**
	 * @param holder Object, with which method will be executed.
	 * @param method Method to execute.
	 */
	public PendingMethod(Method method, Object holder) {
		this.method = method;
		this.holder = holder;
		m_par = method.getParameterTypes();
		label = holder.getClass().getSimpleName() + "." + method.getName() + Integer.toHexString(holder.hashCode());
		starter = method.getAnnotation(Bus.Handler.class).executor();
	}

	/**
	 * Checks if method can be safely executed with given object.
	 */
	public boolean canBeInvokedWith(Object parameter) {

		if (m_par.length != 1)
			return false;

		Class<?> p_type = parameter.getClass();
		Class<?> m_type = m_par[0];

		return m_type.isAssignableFrom(p_type);


	}


	/**
	 * Checks if method can be safely executed with given object list.
	 */
	public boolean canBeInvokedWith(Object... parameters) {

		if (!(parameters.length == m_par.length || method.isVarArgs()))
			return false;

		for (int i = 0; i != parameters.length; i++) {
			Class<?> p_type = parameters[i].getClass();
			Class<?> m_type = m_par[i >= m_par.length ? m_par.length - 1 : i];

			if (!m_type.isAssignableFrom(p_type)) return false;
		}

		return true;
	}

	/**
	 * Invokes method with given parameter list.
	 */
	public void invoke(Object... parameters) {
		try {
			method.invoke(holder, parameters);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Checks if given object exactly matches holder (via ==).
	 */
	public boolean holdersEquals(Object object) {
		return this.holder == object;
	}

}
