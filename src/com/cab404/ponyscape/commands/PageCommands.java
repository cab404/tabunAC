package com.cab404.ponyscape.commands;

import android.util.Log;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.libtabun.data.*;
import com.cab404.libtabun.modules.BlogModule;
import com.cab404.libtabun.modules.LetterLabelModule;
import com.cab404.libtabun.modules.TopicModule;
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
@CommandClass(prefix = "page")
public class PageCommands {

	@Command(command = "load", params = Str.class)
	public void load(final String str) {
		Web.checkNetworkConnection();

		final StaticTextPart loading = new StaticTextPart();
		final String address = str.startsWith("/") ? str : "/blog/" + str;

		Static.bus.send(new Parts.Clear());
		Static.bus.send(new Parts.Add(loading));
		loading.setText("Загружаю список...");

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
						base.bind(new LetterLabelModule(), BLOCK_LETTER_LABEL);
						base.bind(new BlogModule(), BLOCK_BLOG_INFO);
					}

					@Override public void handle(final Object object, final int key) {
						super.handle(object, key);
						switch (key) {
							case BLOCK_TOPIC_HEADER:
								Static.bus.send(new Parts.Add(new TopicPart((Topic) object)));
								break;
							case BLOCK_ERROR:
								Static.bus.send(new Parts.Add(new ErrorPart((TabunError) object)));
								cancel();
								break;
							case BLOCK_PAGINATION:
								Log.v("Page", "Paginator");
								Static.bus.send(new Parts.Add(new PaginatorPart((Paginator) object)));
								break;
							case BLOCK_BLOG_INFO:
								Static.bus.send(new Parts.Add(new BlogPart((Blog) object)));
								break;
							case BLOCK_LETTER_LABEL:
								Log.v("Page", "Label");
								Static.bus.send(new Parts.Add(new LetterLabelPart((LetterLabel) object)));
								break;
						}
					}

					{Static.bus.register(this);}
					@Bus.Handler
					public void cancel(Commands.Abort abort) {
						super.cancel();
						loading.delete();
					}
				};
				try {
					page.fetch(Static.user);
				} catch (MoonlightFail f) {
					Static.bus.send(new Commands.Error("Ошибка при загрузке страницы."));
					Log.w("PageCommands", f);
				}

				Static.bus.unregister(page);
				Static.last_page = page;

				Static.handler.post(new Runnable() {
					@Override public void run() {
						Static.bus.send(new Commands.Clear());
						Static.bus.send(new Commands.Finished());
						loading.delete();
					}
				});
			}
		}).start();

	}

}
