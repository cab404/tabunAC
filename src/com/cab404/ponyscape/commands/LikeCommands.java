package com.cab404.ponyscape.commands;

import android.util.Log;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Bool;
import com.cab404.jconsol.converters.Int;
import com.cab404.libtabun.data.Type;
import com.cab404.libtabun.requests.FavRequest;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;

/**
 * @author cab404
 */
@CommandClass(prefix = "fav")
public class LikeCommands {

	@Command(command = "post", params = {Int.class, Bool.class})
	public void topic(Integer id, Boolean add) {
		fav(Type.TOPIC, id, add);
	}

	@Command(command = "comment", params = {Int.class, Bool.class})
	public void comment(Integer id, Boolean add) {
		fav(Type.COMMENT, id, add);
	}

	@Command(command = "letter", params = {Int.class, Bool.class})
	public void letter(Integer id, Boolean add) {
		fav(Type.COMMENT, id, add);
	}


	void send(Type type, int id, boolean added) {
		switch (type) {
			case COMMENT:
				Static.bus.send(new E.GotData.Fav.Comment(id, added));
				break;
			case TOPIC:
				Static.bus.send(new E.GotData.Fav.Topic(id, added));
				break;
			case TALK:
				Static.bus.send(new E.GotData.Fav.Letter(id, added));
				break;
		}
	}

	void fav(final Type type, final int id, final boolean add) {
		Simple.checkNetworkConnection();

		Static.bus.send(new E.Status("Добавляю в избранное..."));
		final FavRequest request = new FavRequest(type, id, add);
		new Thread(new Runnable() {
			@Override public void run() {
				try {
					request.exec(Static.user);

					if (request.success()) {
						Static.bus.send(new E.Commands.Success(request.msg));
						send(type, id, add);
					} else
						Static.bus.send(new E.Commands.Failure(request.msg));

				} catch (Exception e) {
					Static.bus.send(new E.Commands.Failure("Не удалось изменить список избранного."));
					Log.e("FAV", "ERR", e);
				} finally {
					Static.bus.send(new E.Commands.Clear());
					Static.bus.send(new E.Commands.Finished());
				}
			}
		}).start();

	}

}
