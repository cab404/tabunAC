package com.cab404.ponyscape.commands;

import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.libtabun.data.Blog;
import com.cab404.libtabun.data.Paginator;
import com.cab404.libtabun.data.TabunError;
import com.cab404.libtabun.data.Topic;
import com.cab404.libtabun.modules.BlogModule;
import com.cab404.libtabun.modules.TopicModule;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.moonlight.framework.ModularBlockParser;
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
								Static.bus.send(new Parts.Add(new PaginatorPart((Paginator) object)));
								break;
							case BLOCK_BLOG_INFO:
								Static.bus.send(new Parts.Add(new BlogPart((Blog) object)));
								break;

						}
					}

					{Static.bus.register(this);}
					@Bus.Handler
					public void cancel(Commands.Abort abort) {
						super.cancel();
					}
				};
				page.fetch(Static.user);
				Static.last_page = page;

				Static.handler.post(new Runnable() {
					@Override public void run() {
						Static.bus.send(new Commands.Clear());
						Static.bus.send(new Commands.Finished());
						Static.bus.send(new Parts.Remove(loading));
					}
				});
			}
		}).start();

	}

}
