package com.cab404.ponyscape.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.cab404.moonlight.util.RU;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;

import java.util.List;

/**
 * @author cab404
 */
public class AbstractActivity extends Activity {

	static final boolean THEMES_ENABLED = Static.cfg.ensure("main.themes_enabled", false);

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Static.bus.register(this);
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		Static.bus.unregister(this);
	}

	@Override protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(
				THEMES_ENABLED ?
						Static.theme.getContext(newBase) : newBase
		);
	}

	/**
	 * Тут происходит весь резолвинг адресов.
	 */
	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {

			Uri data = intent.getData();
			Log.v("Main", "Получили новый Intent по адресу " + data);
			List<String> segments = data.getPathSegments();
			String command = null;

			/* Корневой адрес */
			if (segments.size() == 0)
				command = "page load /";

			if (segments.size() == 2) {

				/* Обработка постов */
				if ("blog".equals(segments.get(0)))
					command = "page load " + segments.get(1);

				/* Обработка профилей */
				if ("profile".equals(segments.get(0)))
					command = "user load " + segments.get(1);

				/* Обработка ссылок на комментарии  */
				if ("comments".equals(segments.get(0)))
					command = "post by_comment " + segments.get(1);

			}
			if (segments.size() == 3) {
				if ("blog".equals(segments.get(0))) {

					/* Обработка приглашений в блоги */
					if (segments.get(2).equals("accept"))
						new Thread() {
							@Override public void run() {
								Simple.checkNetworkConnection();
								HttpHead accept = new HttpHead(intent.getData().toString());
								HttpResponse response = RU.exec(accept, Static.user);
								if (response.getStatusLine().getStatusCode() / 100 < 4)
									Static.bus.send(new E.Commands.Success("Приглашение принято."));
								else
									Static.bus.send(new E.Commands.Failure("Ошибка при принятии приглашения : "
											+ response.getStatusLine().getStatusCode()));
							}
						}.start();
					else if (segments.get(2).equals("reject"))
						new Thread() {
							@Override public void run() {
								Simple.checkNetworkConnection();
								HttpHead accept = new HttpHead(intent.getData().toString());
								HttpResponse response = RU.exec(accept, Static.user);
								if (response.getStatusLine().getStatusCode() / 100 < 4)
									Static.bus.send(new E.Commands.Success("Приглашение отвергнуто."));
								else
									Static.bus.send(new E.Commands.Failure("Ошибка при отказе от приглашения : "
											+ response.getStatusLine().getStatusCode()));
							}
						}.start();
					else

					/* Обработка постов */
						command = "post load " + segments.get(2).replace(".html", "");
				}
			}

			if (command != null)
				Static.bus.send(new E.Commands.Run(command));

		} else
			super.onNewIntent(intent);
	}

}
