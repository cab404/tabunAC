package com.cab404.ponyscape.utils.views;

import android.app.Activity;
import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * Inserts views into fields.
 *
 * @author cab404
 */
public class ViewSugar {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Bind {
		int value();
	}

	public static void bind(Object object, View view) {
		for (Field field : object.getClass().getDeclaredFields()) {
			Bind annotation = field.getAnnotation(Bind.class);
			if (annotation != null) {
				field.setAccessible(true);
				try {
					field.set(object, view.findViewById(annotation.value()));
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				field.setAccessible(false);
			}
		}
	}

	public static void bind(Activity view) {
		for (Field field : view.getClass().getDeclaredFields()) {
			Bind annotation = field.getAnnotation(Bind.class);
			if (annotation != null) {
				field.setAccessible(true);
				try {
					field.set(view, view.findViewById(annotation.value()));
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				field.setAccessible(false);
			}
		}
	}

}
