package com.cab404.ponyscape.commands;

import android.util.Log;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Int;
import com.cab404.libtabun.data.Letter;
import com.cab404.libtabun.data.TabunError;
import com.cab404.libtabun.pages.LetterPage;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.libtabun.requests.LetterAddRequest;
import com.cab404.moonlight.util.SU;
import com.cab404.moonlight.util.exceptions.MoonlightFail;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.parts.CommentListPart;
import com.cab404.ponyscape.parts.EditorPart;
import com.cab404.ponyscape.parts.ErrorPart;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.sjbus.Bus;

import java.util.List;

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
		Static.bus.send(new E.Commands.Finished());
		Static.bus.send(new E.Commands.Run("page load /talk/inbox"));
	}


	@Command(command = "load", params = Int.class)
	public void post(final Integer id) {
		Simple.checkNetworkConnection();

		new Thread(new Runnable() {
			@Override public void run() {

				final CommentListPart list = new CommentListPart(id, true);
				Static.bus.send(new E.Parts.Run(list, false));

				TabunPage page = new LetterPage(id) {

					@Override public void handle(final Object object, final int key) {
						super.handle(object, key);

						switch (key) {
							case BLOCK_LETTER_HEADER:
								final Letter letter = (Letter) object;
								Static.handler.post(new Runnable() {
									@Override public void run() {
										list.add(letter);
									}
								});
								break;
							case BLOCK_COMMENT:
								Static.handler.post(new Runnable() {
									@Override public void run() {
										list.add((com.cab404.libtabun.data.Comment) object);
										list.update();
									}
								});
								break;
							case BLOCK_ERROR:
								Static.bus.send(new E.Parts.Run(new ErrorPart((TabunError) object), true));
								cancel();
								break;
						}
					}

					{Static.bus.register(this);}
					@Bus.Handler
					public void cancel(E.Commands.Abort abort) {
						super.cancel();
					}
				};
				try {
					page.fetch(Static.user);
				} catch (MoonlightFail f) {
					Static.bus.send(new E.Commands.Error("Ошибка при загрузке письма."));
					Log.w("PageCommands", f);
				}
				Static.bus.unregister(page);
				Static.last_page = page;

				Static.bus.send(new E.Commands.Clear());
				Static.bus.send(new E.Commands.Finished());
			}
		}).start();
	}


	@Command(command = "write")
	public void write() {

		EditorPart part = new EditorPart(
				"Пишем пост",
				"Заголовок\n=====\nАдресаты\n=====\nТекст",
				new EditorPart.EditorActionHandler() {
					@Override public boolean finished(final CharSequence text) {
						List<String> split = SU.split(text.toString(), "\n=====");

						if (split.size() == 3) {
							final Letter letter = new Letter();
							letter.title = split.get(0);
							letter.text = split.get(2);
							letter.recipients = SU.split(split.get(1), ",");
							final EditorPart.EditorActionHandler self = this;

							new Thread() {

								@Override public void run() {
									LetterAddRequest request = new LetterAddRequest(letter);
									request.exec(Static.user);
									if (letter.id != 0) {
										Static.bus.send(new E.Commands.Success("Yay, письмо написано. ID:" + letter.id));
										Static.bus.send(new E.Commands.Finished());
										Static.bus.send(new E.Commands.Run("mail load " + letter.id));
									} else {
										Static.bus.send(new E.Commands.Success("Не удалось создать письмо :("));

										Static.bus.send(
												new E.Parts.Run(
														new EditorPart(
																"Пишем письмо",
																text,
																self
														)
														, true)
										);
									}

									super.run();
								}

							}.start();

							return true;
						} else {
							Static.bus.send(new E.Commands.Error("Не все части письма найдены: " +
									"проверьте, разделены ли адресаты, текст и заголовок '====='"));
							return false;
						}

					}

					@Override public void cancelled() {
						Static.bus.send(new E.Commands.Finished());
					}
				});

		Static.bus.send(new E.Parts.Run(part, true));
	}


}
