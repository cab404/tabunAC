package com.cab404.ponyscape.utils.notifications;

import java.util.concurrent.Delayed;

/**
 * @author cab404
 */
public abstract class Notifier implements Delayed {

	public abstract void check(Notificator notificator);

	public abstract void dismissed();

}
