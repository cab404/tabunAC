package com.cab404.ponyscape.utils.notifications;

import android.app.Notification;
import android.content.Context;

import java.util.concurrent.Delayed;

/**
 * @author cab404
 */
public abstract class Notifier implements Delayed {

	public abstract Notification check(Context context);

	public abstract void dismissed();

}
