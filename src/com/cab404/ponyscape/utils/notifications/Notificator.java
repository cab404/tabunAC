package com.cab404.ponyscape.utils.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author cab404
 */
public class Notificator {
	private final Handler handler;
	private final Context context;
	public final Map<String, Notifier> notifiers;
	private int root;
	private NotificationManager man;


	public Notificator(Context context) {
		this.context = context;
		handler = new Handler(context.getMainLooper());
		man = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifiers = new ConcurrentHashMap<>();
	}


	public void notify(Notification notification) {
		man.notify(0, notification);
	}


	private void step() {
		for (Map.Entry<String, Notifier> e : notifiers.entrySet()) {
			Notification check = e.getValue().check(context);
			if (check != null)
				man.notify(e.getKey().hashCode(), check);
		}
	}

}
