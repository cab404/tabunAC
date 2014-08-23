package com.cab404.ponyscape.commands;

import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Int;
import com.cab404.libtabun.data.TabunError;
import com.cab404.libtabun.data.Topic;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.libtabun.pages.TopicPage;
import com.cab404.ponyscape.bus.events.Commands;
import com.cab404.ponyscape.bus.events.Parts;
import com.cab404.ponyscape.parts.CommentListPart;
import com.cab404.ponyscape.parts.ErrorPart;
import com.cab404.ponyscape.parts.StaticTextPart;
import com.cab404.ponyscape.parts.TopicPart;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.Web;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */
@CommandClass(prefix = "post")
public class PostCommands {

	@Command(command = "load", params = Int.class)
	public void post(final Integer id) {
		Web.checkNetworkConnection();

		final StaticTextPart loading = new StaticTextPart();
		Static.bus.send(new Parts.Clear());
		Static.bus.send(new Parts.Add(loading));
		loading.setText("Загружаю пост...");

		Static.history.add("post load " + id);

		new Thread(new Runnable() {
			@Override public void run() {


				TabunPage page = new TopicPage(id) {
					CommentListPart list;
					int all = 0;
					int num = 0;

					@Override public void handle(final Object object, final int key) {

						super.handle(object, key);
						switch (key) {
							case BLOCK_TOPIC_HEADER:
								final TopicPart topicPart = new TopicPart((Topic) object);
								list = new CommentListPart(topicPart);
								Static.handler.post(new Runnable() {
									@Override public void run() {
										Static.bus.send(new Parts.Add(topicPart));
										Static.bus.send(new Parts.Add(list));
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
								Static.handler.post(new Runnable() {
									@Override public void run() {
										Static.bus.send(new Parts.Add(new ErrorPart((TabunError) object)));
									}
								});
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
				page.fetch(Static.user);
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
