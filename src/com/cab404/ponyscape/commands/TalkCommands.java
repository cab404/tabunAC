package com.cab404.ponyscape.commands;

import android.util.Log;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Int;
import com.cab404.libtabun.data.Letter;
import com.cab404.libtabun.data.TabunError;
import com.cab404.libtabun.pages.LetterPage;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.moonlight.util.exceptions.MoonlightFail;
import com.cab404.ponyscape.bus.events.Commands;
import com.cab404.ponyscape.bus.events.Parts;
import com.cab404.ponyscape.parts.CommentListPart;
import com.cab404.ponyscape.parts.ErrorPart;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */
@CommandClass(prefix = "mail")
public class TalkCommands {

	/**
	 * Просто перенаправляем в page.
	 */
	@Command(command = "box")
	public void list() {
		Static.history.add("mail box");
		Static.bus.send(new Commands.Finished());
		Static.bus.send(new Commands.Run("page load /talk/inbox"));
	}


	@Command(command = "load", params = Int.class)
	public void post(final Integer id) {
		Simple.checkNetworkConnection();

		new Thread(new Runnable() {
			@Override public void run() {

				final CommentListPart list = new CommentListPart(id, true);
				Static.bus.send(new Parts.Run(list, false));

				TabunPage page = new LetterPage(id) {

					@Override public void handle(final Object object, final int key) {
						super.handle(object, key);

						switch (key) {
							case BLOCK_LETTER_HEADER:
								Log.v("Talk", "Header");
								final Letter letter = (Letter) object;
								Static.handler.post(new Runnable() {
									@Override public void run() {
										list.add(letter);
									}
								});
								break;
							case BLOCK_COMMENT:
								Log.v("Talk", "Comment");
								Static.handler.post(new Runnable() {
									@Override public void run() {
										list.add((com.cab404.libtabun.data.Comment) object);
										list.update();
									}
								});
								break;
							case BLOCK_ERROR:
								Static.bus.send(new Parts.Run(new ErrorPart((TabunError) object), true));
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
				} catch (MoonlightFail f) {
					Static.bus.send(new Commands.Error("Ошибка при загрузке письма."));
					Log.w("PageCommands", f);
				}
				Static.bus.unregister(page);
				Static.last_page = page;

				Static.bus.send(new Commands.Clear());
				Static.bus.send(new Commands.Finished());
			}
		}).start();
	}

}
