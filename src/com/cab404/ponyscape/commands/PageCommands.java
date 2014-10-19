package com.cab404.ponyscape.commands;

import android.util.Log;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.libtabun.data.*;
import com.cab404.libtabun.modules.BlogModule;
import com.cab404.libtabun.modules.CommentModule;
import com.cab404.libtabun.modules.LetterLabelModule;
import com.cab404.libtabun.modules.TopicModule;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.moonlight.framework.ModularBlockParser;
import com.cab404.moonlight.util.exceptions.MoonlightFail;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.parts.*;
import com.cab404.ponyscape.parts.raw_text.ErrorPart;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */
@CommandClass(prefix = "page")
public class PageCommands {

	@Command(command = "load", params = Str.class)
	public void load(final String str) {
		Simple.checkNetworkConnection();

		final String address = str.startsWith("/") ? str : "/blog/" + str;

		Static.bus.send(new E.Status("Загружаю страницу..."));

		Static.history.add("page load " + str);

		new Thread(new Runnable() {
			@Override public void run() {

				TabunPage page = new TabunPage() {


					@Override public String getURL() {
						return address;
					}

					@Override protected void bindParsers(ModularBlockParser base) {
						super.bindParsers(base);
						base.bind(new TopicModule(TopicModule.Mode.LIST), BLOCK_TOPIC_HEADER);
						base.bind(new CommentModule(CommentModule.Mode.LIST), BLOCK_COMMENT);
						base.bind(new LetterLabelModule(), BLOCK_LETTER_LABEL);
						base.bind(new BlogModule(), BLOCK_BLOG_INFO);
					}

					boolean cleared = false;
					private void clearOnDemand() {
						if (!cleared) {
							cleared = true;
							Static.bus.send(new E.Parts.Clear());
						}
					}

					private boolean letters = false;
					@Override public void handle(final Object object, final int key) {
						super.handle(object, key);
						switch (key) {
							case BLOCK_TOPIC_HEADER:
								clearOnDemand();
								Static.bus.send(new E.Parts.Add(new TopicPart((Topic) object)));
								break;
							case BLOCK_COMMENT:
								clearOnDemand();
								Static.bus.send(new E.Parts.Add(new CommentPart((Comment) object, false)));
								break;
							case BLOCK_PAGINATION:
								clearOnDemand();
								Static.bus.send(new E.Parts.Add(new PaginatorPart((Paginator) object)));
								break;
							case BLOCK_BLOG_INFO:
								clearOnDemand();
								Static.bus.send(new E.Parts.Add(new BlogPart((Blog) object)));
								break;
							case BLOCK_LETTER_LABEL:
								clearOnDemand();
								if (!letters) {
									letters = true;
									Static.bus.send(new E.Parts.Add(new MailboxController()));
								}
								Static.bus.send(new E.Parts.Add(new LetterLabelPart((LetterLabel) object)));
								break;
							case BLOCK_ERROR:
								Static.bus.send(new E.Parts.Add(new ErrorPart((TabunError) object)));
								cancel();
								break;
						}
					}

					{Static.bus.register(this);}
					@Bus.Handler
					public void cancel(E.Commands.Abort abort) {
						super.cancel();
//						loading.delete();
					}
				};
				try {
					page.fetch(Static.user);
				} catch (MoonlightFail f) {
					Static.bus.send(new E.Commands.Failure("Ошибка при загрузке страницы."));
					Log.w("PageCommands", f);
				}

				Static.bus.unregister(page);
				Static.last_page = page;

				Static.handler.post(new Runnable() {
					@Override public void run() {
						Static.bus.send(new E.Commands.Clear());
						Static.bus.send(new E.Commands.Finished());
//						loading.delete();
					}
				});
			}
		}).start();

	}

	@Command(command = "top", params = Str.class)
	public void loadFirst(String str) {
		Simple.checkNetworkConnection();

		Static.history.add("page top " + str);
		final String address = str.startsWith("/") ? str : "/blog/" + str;
		Static.bus.send(new E.Status("Загружаю страницу..."));

		new Thread(new Runnable() {
			@Override public void run() {

				TabunPage page = new TabunPage() {
					@Override public String getURL() {
						return address;
					}

					@Override protected void bindParsers(ModularBlockParser base) {
						super.bindParsers(base);
						base.bind(new TopicModule(TopicModule.Mode.LIST), BLOCK_TOPIC_HEADER);
					}

					private boolean letters = false;
					@Override public void handle(final Object object, final int key) {
						super.handle(object, key);
						switch (key) {
							case BLOCK_TOPIC_HEADER:
								cancel();
								Simple.redirect("post load " + ((Topic) object).id);
								break;
							case BLOCK_ERROR:
								Static.bus.send(new E.Parts.Add(new ErrorPart((TabunError) object)));
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
					Static.bus.send(new E.Commands.Failure("Ошибка при загрузке страницы."));
					Log.w("PageCommands", f);
				}

				Static.bus.unregister(page);
				Static.last_page = page;

				Static.handler.post(new Runnable() {
					@Override public void run() {
						Static.bus.send(new E.Commands.Clear());
						Static.bus.send(new E.Commands.Finished());
					}
				});

			}
		}).start();

	}

}
