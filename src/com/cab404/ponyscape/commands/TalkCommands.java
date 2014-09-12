package com.cab404.ponyscape.commands;

import android.util.Log;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Int;
import com.cab404.libtabun.data.*;
import com.cab404.libtabun.modules.PaginatorModule;
import com.cab404.libtabun.pages.LetterPage;
import com.cab404.libtabun.pages.LetterTablePage;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.moonlight.framework.ModularBlockParser;
import com.cab404.moonlight.util.exceptions.MoonlightFail;
import com.cab404.ponyscape.bus.events.Commands;
import com.cab404.ponyscape.bus.events.Parts;
import com.cab404.ponyscape.parts.*;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.Web;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */
@CommandClass(prefix = "mail")
public class TalkCommands {

	@Command(command = "box")
	public void list() {

		Web.checkNetworkConnection();

		final StaticTextPart loading = new StaticTextPart();
		Static.bus.send(new Parts.Clear());
		Static.bus.send(new Parts.Add(loading));
		loading.setText("Загружаю список...");

		Static.history.add("mail box");

		new Thread(new Runnable() {
			@Override public void run() {


				TabunPage page = new LetterTablePage() {
					@Override public void handle(final Object object, final int key) {

						super.handle(object, key);
						switch (key) {
							case BLOCK_LETTER_LABEL:
								Static.bus.send(new Parts.Add(new LetterLabelPart((LetterLabel) object)));
								break;
							case BLOCK_ERROR:
								Static.bus.send(new Parts.Add(new ErrorPart((TabunError) object)));
								cancel();
								break;
							case BLOCK_PAGINATION:
								Static.bus.send(new Parts.Add(new PaginatorPart((Paginator) object)));
								break;
						}
					}

					@Override protected void bindParsers(ModularBlockParser base) {
						super.bindParsers(base);
						base.bind(new PaginatorModule(), BLOCK_PAGINATION);
					}
					{Static.bus.register(this);}
					@Bus.Handler
					public void cancel(Commands.Abort abort) {
						super.cancel();
						Static.bus.send(new Parts.Remove(loading));
					}
				};
				try {
					page.fetch(Static.user);
				} catch (MoonlightFail f) {
					Static.bus.send(new Commands.Error("Ошибка при загрузке списка."));
					Log.w("TalkCommands", f);
				}
				Static.bus.unregister(page);
				Static.last_page = page;

				Static.handler.post(new Runnable() {
					@Override public void run() {
						loading.delete();
						Static.bus.send(new Commands.Clear());
						Static.bus.send(new Commands.Finished());
					}
				});
			}
		}).start();
	}


	@Command(command = "load", params = Int.class)
	public void post(final Integer id) {
		Web.checkNetworkConnection();

		final StaticTextPart loading = new StaticTextPart();
		Static.bus.send(new Parts.Clear());
		Static.bus.send(new Parts.Add(loading));
		loading.setText("Загружаю письмо " + id + "...");

		Static.history.add("mail load " + id);

		new Thread(new Runnable() {
			@Override public void run() {


				TabunPage page = new LetterPage(id) {
					CommentListPart list;
					int all = 0;
					int num = 0;

					@Override public void handle(final Object object, final int key) {
						super.handle(object, key);

						if (list == null) {
							list = new CommentListPart(id, false);
							Static.bus.send(new Parts.Run(list, false));
						}

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
								num++;
								Static.handler.post(new Runnable() {
									@Override public void run() {
										loading.setText("Загружено " + num + " из " + all + " комментариев.");
									}
								});
								break;
							case BLOCK_COMMENT_NUM:
								all = (Integer) object;
								break;
							case BLOCK_ERROR:
								Static.bus.send(new Parts.Add(new ErrorPart((TabunError) object)));
								cancel();
								break;
						}
					}

					{Static.bus.register(this);}
					@Bus.Handler
					public void cancel(Commands.Abort abort) {
						super.cancel();
						Static.bus.send(new Parts.Remove(loading));
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

				Static.handler.post(new Runnable() {
					@Override public void run() {
						loading.delete();
						Static.bus.send(new Commands.Clear());
						Static.bus.send(new Commands.Finished());
					}
				});
			}
		}).start();
	}

}
