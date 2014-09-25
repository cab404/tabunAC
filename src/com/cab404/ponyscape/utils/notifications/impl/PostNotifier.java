package com.cab404.ponyscape.utils.notifications.impl;

import android.app.Notification;
import android.content.Context;
import com.cab404.ponyscape.utils.notifications.Notifier;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author cab404
 */
public class PostNotifier extends Notifier {

	private final int id;
	private final int last_comment;

	public PostNotifier(int id, int last_comment) {
		this.id = id;
		this.last_comment = last_comment;
	}

	@Override public Notification check(Context context) {


//		if (Build.VERSION.SDK_INT >= 11) {
//			Notification notification = new Notification.Builder(context)
//
//					.setContentTitle("Новые ответы в отслеживаемом посте")
//					.setContentText("+" + num + " " + context.getResources().getQuantityString(R.plurals.new_comments, num))
//					.setTicker("Новые ответы в отслеживаемом посте")
//
//					.setAutoCancel(true)
//					.setNumber(num)
//					.setSmallIcon(R.drawable.ic_notification)
//					.build();
//		}
		return null;
	}

	@Override public void dismissed() {

	}
	@Override public long getDelay(TimeUnit unit) {
		return 0;
	}
	@Override public int compareTo(Delayed another) {
		return 0;
	}
}
