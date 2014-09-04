package com.cab404.ponyscape.commands;

import android.util.Log;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.libtabun.data.Profile;
import com.cab404.libtabun.data.TabunError;
import com.cab404.libtabun.pages.ProfilePage;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.moonlight.util.exceptions.LoadingFail;
import com.cab404.moonlight.util.exceptions.MoonlightFail;
import com.cab404.ponyscape.bus.events.Commands;
import com.cab404.ponyscape.bus.events.Parts;
import com.cab404.ponyscape.parts.ProfilePart;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.Web;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */
@CommandClass(prefix = "user")
public class UserCommands {

	@Command(command = "load", params = Str.class)
	public static void load(final String name) {
		Web.checkNetworkConnection();

		new Thread(new Runnable() {
			@Override public void run() {

				TabunPage page = new ProfilePage(name) {

					@Override public void handle(final Object object, final int key) {

						super.handle(object, key);
						switch (key) {
							case BLOCK_USER_INFO:
								final ProfilePart part = new ProfilePart((Profile) object);
								Static.handler.post(new Runnable() {
									@Override public void run() {
										Static.bus.send(new Parts.Run(part));
									}
								});
								break;
							case BLOCK_ERROR:
								switch ((TabunError) object) {
									case NOT_FOUND:
										Static.bus.send(new Commands.Error("Пользователя не существует."));
										break;
									default:
										Static.bus.send(new Commands.Error("Произошла НЁХ. Свяжитесь со спецлужбами."));
										break;
								}
								cancel();
								break;
						}
					}

					{Static.bus.register(this);}
					@Bus.Handler
					public void cancel(Commands.Abort abort) {
						super.cancel();
					}
				};
				try {
					page.fetch(Static.user);
				} catch (LoadingFail f) {
					Log.w("Profile", name + ": Нет таких");
				} catch (MoonlightFail f) {
					Static.bus.send(new Commands.Error("Ошибка при запросе данных пользователя."));
					Log.w("UserInfo", f);
				}
				Static.last_page = page;

				Static.handler.post(new Runnable() {
					@Override public void run() {
						Static.bus.send(new Commands.Clear());
						Static.bus.send(new Commands.Finished());
					}
				});
			}
		}).start();
	}

}
