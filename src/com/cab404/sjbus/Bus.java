package com.cab404.sjbus;

import android.util.Log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

/**
 * Simple bus
 *
 * @author cab404
 */
public class Bus {

	public boolean log = false;

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Handler {
		public Class<? extends Executor> executor() default BlockingExecutor.class;
	}

	private final List<PendingMethod> handlers;
	private final HashMap<Class<? extends Executor>, Executor> executors;

	public Bus() {
		handlers = new CopyOnWriteArrayList<>();
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

	private void log(String str) {
		log("SiJBus", str);
	}

	private void log(String tag, String str) {
		if (log)
			Log.v(tag, str);
	}

	public void register(Object obj) {
		log("Registered object of class " + obj.getClass() + ", inst. " + Integer.toHexString(obj.hashCode()));
		for (Method method : obj.getClass().getMethods())
			if (method.getAnnotation(Handler.class) != null)
				handlers.add(new PendingMethod(method, obj));
		log(handlers.size() + " handlers now");
	}


	public void send(final Object event) {
		boolean something_was_invoked = false;

		final String log_session = Integer.toHexString((int) (Math.random() * (Math.pow(16, 4) - Math.pow(16, 3)) + Math.pow(16, 3)));

		if (log)
			Log.v("SiJBus:Send:" + log_session, "Sent event of class " + event.getClass() + ", inst. " + Integer.toHexString(event.hashCode()));
		for (final PendingMethod method : handlers)
			if (method.canBeInvokedWith(event)) {
				something_was_invoked = true;
				final Executor executor = getExecutor(method.starter);
				executor.execute(new Runnable() {
					@Override public void run() {
						try {
							log("SiJBus:Send:" + log_session, "Invoking handler " + method.label);
							method.invoke(event);
						} catch (Throwable t) {
							throw new RuntimeException(
									"Exception while executing bus handler `"
											+ method.label
											+ "` via `"
											+ executor.getClass().getSimpleName()
											+ "`",
									t);
						}
					}
				});
			}
		if (!something_was_invoked)
			log("SiJBus:Send:" + log_session, "No handlers was invoked on  event of class " + event.getClass() + ", inst. " + Integer.toHexString(event.hashCode()));
	}

	public void unregister(Object obj) {
		log("Unregistered object of class " + obj.getClass() + ", inst. " + Integer.toHexString(obj.hashCode()));

		for (int i = 0; i < handlers.size(); ) {
			// Removing object if it is matching given
			if (handlers.get(i).holdersEquals(obj))
				handlers.remove(i);
			else
				i++;
		}

		log(handlers.size() + " handlers now");
	}

}
