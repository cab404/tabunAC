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


	void fav(final Type type, final int id, boolean add) {
		Simple.checkNetworkConnection();

		final FavRequest request = new FavRequest(type, id, add);
		new Thread(new Runnable() {
			@Override public void run() {
				try {
					request.exec(Static.user);

					if (request.success()) {
						Static.bus.send(new E.Commands.Success(request.msg));
					} else
						Static.bus.send(new E.Commands.Error(request.msg));

				} catch (Exception e) {
					Static.bus.send(new E.Commands.Error("Не удалось изменить список избранного."));
					Log.e("FAV", "ERR", e);
				} finally {
					Static.bus.send(new E.Commands.Clear());
					Static.bus.send(new E.Commands.Finished());
				}
			}
		}).start();

	}

}
