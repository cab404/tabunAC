package com.cab404.sjbus;

import android.util.Log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Simple bus
 *
 * @author cab404
 */
public class Bus {

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Handler {
		public Class<? extends Executor> executor() default BlockingExecutor.class;
	}

	private final List<PendingMethod> handlers;
	private final HashMap<Class<? extends Executor>, Executor> executors;

	public Bus() {
		handlers = new ArrayList<>();
		executors = new HashMap<>();
	}

	private Executor getExecutor(Class<? extends Executor> clazz) {
		if (executors.containsKey(clazz)) {
			return executors.get(clazz);
		} else {
			try {
				Constructor<? extends Executor> constructor = clazz.getConstructor();
				executors.put(clazz, constructor.newInstance());
				return executors.get(clazz);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("No clean constructor found for given executor!", e);
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("Cannot create executor!", e);
			}
		}

	}

	public void register(Object obj) {
		Log.v("SiJBus", "Registered object of class " + obj.getClass() + ", inst. " + Integer.toHexString(obj.hashCode()));
		unregister(obj);
		for (Method method : obj.getClass().getMethods())
			if (method.getAnnotation(Handler.class) != null)
				handlers.add(new PendingMethod(method, obj));
	}


	public void send(final Object event) {
		boolean something_was_invoked = false;

		final String log_session = Integer.toHexString((int) (Math.random() * (Math.pow(16, 4) - Math.pow(16, 3)) + Math.pow(16, 3)));

		Log.v("SiJBus:Send:" + log_session, "Sent event of class " + event.getClass() + ", inst. " + Integer.toHexString(event.hashCode()));
		for (final PendingMethod method : handlers)
			if (method.canBeInvokedWith(event)) {
				something_was_invoked = true;
				final Executor executor = getExecutor(method.starter);
				executor.execute(new Runnable() {
					@Override public void run() {
						try {
							Log.v("SiJBus:Send:" + log_session, "Invoking handler " + method.method.toGenericString() + " in object " + Integer.toHexString(method.holder.hashCode()));
							method.invoke(event);
						} catch (Throwable t) {
							throw new RuntimeException(
									"Exception while executing bus handler `"
											+ method.method.toGenericString()
											+ "` via `"
											+ executor.getClass().getSimpleName()
											+ "`",
									t);
						}
					}
				});
			}
		if (!something_was_invoked)
			Log.v("SiJBus:Send:" + log_session, "No handlers was invoked on  event of class " + event.getClass() + ", inst. " + Integer.toHexString(event.hashCode()));
	}

	public void unregister(Object obj) {
		for (int i = 0; i < handlers.size(); i++)
			if (handlers.get(i).holder.equals(obj))
				handlers.remove(i);
			else
				i++;
	}

}
