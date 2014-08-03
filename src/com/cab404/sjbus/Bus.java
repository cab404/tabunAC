package com.cab404.sjbus;

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

	/**
	 * @author cab404
	 */
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
		unregister(obj);
		for (Method method : obj.getClass().getMethods())
			if (method.getAnnotation(Handler.class) != null)
				handlers.add(new PendingMethod(method, obj));
	}


	public void send(final Object event) {
		for (final PendingMethod method : handlers)
			if (method.canBeInvokedWith(event)) {
				final Executor executor = getExecutor(method.starter);
				executor.execute(new Runnable() {
					@Override public void run() {
						try {
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
	}

	public void unregister(Object obj) {
		for (int i = 0; i < handlers.size(); i++)
			if (handlers.get(i).holder.equals(obj))
				handlers.remove(i);
			else
				i++;
	}

}
