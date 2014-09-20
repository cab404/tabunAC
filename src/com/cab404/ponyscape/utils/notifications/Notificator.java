package com.cab404.ponyscape.utils.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import com.cab404.ponyscape.R;


/**
 * @author cab404
 */
public class Notificator {
	private final Handler handler;
	private final Context context;
	private int root;
	private NotificationManager man;


	public Notificator(Context context) {
		this.context = context;
		handler = new Handler(context.getMainLooper());
		man = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}


	public void notifyNewComments(String post, int num) {

		if (Build.VERSION.SDK_INT >= 11) {
			Notification notification = new Notification.Builder(context)

					.setContentTitle("Новые ответы в отслеживаемом посте")
					.setContentText("+" + num + " " + context.getResources().getQuantityString(R.plurals.new_comments, num))
					.setTicker("Новые ответы в отслеживаемом посте")

					.setAutoCancel(true)
					.setNumber(num)
					.setSmallIcon(R.drawable.ic_notification)
					.build();

			man.notify(0, notification);
		}
	}

	public void postNotifier(Notifier notifier) {}

}
