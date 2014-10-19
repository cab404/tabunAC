package com.cab404.ponyscape.commands;

import android.util.Log;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Int;
import com.cab404.libtabun.data.Blog;
import com.cab404.libtabun.data.Comment;
import com.cab404.libtabun.data.TabunError;
import com.cab404.libtabun.data.Topic;
import com.cab404.libtabun.pages.TopicPage;
import com.cab404.libtabun.requests.TopicAddRequest;
import com.cab404.moonlight.framework.AccessProfile;
import com.cab404.moonlight.framework.Request;
import com.cab404.moonlight.util.SU;
import com.cab404.moonlight.util.exceptions.MoonlightFail;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.parts.CommentListPart;
import com.cab404.ponyscape.parts.editor.EditorPart;
import com.cab404.ponyscape.parts.raw_text.ErrorPart;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.sjbus.Bus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.LinkedList;
import java.util.List;

/**
 * @author cab404
 */
@CommandClass(prefix = "post")
public class PostCommands {

	@Command(command = "load", params = Int.class)
	public void post(final Integer id) {
		post(id, -1);
	}


	@Command(command = "by_comment", params = Int.class)
	public void postByComment(final Integer id) {
		if (id < 0) {
			Static.bus.send(new E.Commands.Finished());
			return;
		}
		new Thread(new Runnable() {
			@Override public void run() {
				new Request() {
					@Override protected HttpRequestBase getRequest(AccessProfile accessProfile) {
						return new HttpHead("/comments/" + id);
					}
					@Override protected void onResponseGain(HttpResponse response) {
						if (response.getStatusLine().getStatusCode() / 100 >= 4) {
							cancel();
							Static.bus.send(new E.Commands.Failure("Ошибка " + response.getStatusLine().getStatusCode()));
							Static.bus.send(new E.Commands.Finished());
						}
					}
					@Override protected void onRedirect(String to) {
						Log.v("REDIRECT", to);
						String address = to.substring(to.lastIndexOf('/') + 1);
						List<String> split = SU.split(address, ".html#comment");
						int post = Integer.valueOf(split.get(0));
						int comment = Integer.valueOf(split.get(1));

						Static.bus.send(new E.Commands.Finished());
						Static.bus.send(new E.Commands.Run("post load " + post + " " + comment));

						cancel();
					}
					@Override public void finished() {
						Static.bus.send(new E.Commands.Finished());
					}
				}.fetch(Static.user);
			}
		}).start();

	}


	@Command(command = "load", params = {Int.class, Int.class})
	public void post(final Integer id, final Integer focusOn) {
		Simple.checkNetworkConnection();

		new Thread(new Runnable() {
			@Override public void run() {

				final CommentListPart list = new CommentListPart(id, false);
				Static.bus.send(new E.Parts.Run(list, false));

				final TopicPage page = new TopicPage(id) {

					@Override public void handle(final Object object, final int key) {
						switch (key) {
							case BLOCK_TOPIC_HEADER:
								final Topic topic = (Topic) object;
								Static.handler.post(new Runnable() {
									@Override public void run() {
										list.add(topic);
									}
								});
								break;

							case BLOCK_COMMENT:
								Comment comment = (Comment) object;
								comments.add(comment);

								if (!comment.deleted)
									Static.bus.send(new E.Status("Комментарий от " + comment.author.login));

								if (comments.size() > 50) {
									final List<Comment> dump = comments;
									comments = new LinkedList<>();
									Static.handler.post(new Runnable() {
										@Override public void run() {
											for (Comment comment : dump)
												list.add(comment);
											list.update();
										}
									});
								}
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

					Static.handler.post(new Runnable() {
						@Override public void run() {
							while (!page.comments.isEmpty())
								list.add(page.comments.remove(0));
							list.update();
						}
					});

				} catch (MoonlightFail f) {
					Static.bus.send(new E.Commands.Failure("Ошибка при загрузке поста."));
					Log.w("PageCommands", f);
				}

				if (focusOn != -1)
					Static.handler.post(new Runnable() {
						@Override public void run() {
							list.select(list.indexOf(focusOn), -5000);
						}
					});

				Static.bus.unregister(page);
				Static.last_page = page;

				Static.bus.send(new E.Commands.Clear());
				Static.bus.send(new E.Commands.Finished());
			}
		}).start();
	}

	@Command(command = "write", params = {Int.class})
	public void write(final Integer blogID) {

		EditorPart part = new EditorPart(
				"Пишем пост",
				"Заголовок\n=====\nТекст\n=====\nэтот тег кто-то прочитал",
				new EditorPart.EditorActionHandler() {
					@Override public boolean finished(final CharSequence text) {
						List<String> split = SU.split(text.toString(), "\n=====");

						if (split.size() == 3) {
							final Topic topic = new Topic();
							topic.title = split.get(0);
							topic.text = split.get(1);
							topic.tags = SU.split(split.get(2), ",");
							topic.blog = new Blog();
							topic.blog.id = blogID;
							final EditorPart.EditorActionHandler self = this;

							new Thread() {

								@Override public void run() {
									TopicAddRequest request = new TopicAddRequest(topic);
									request.exec(Static.user);
									if (topic.id != 0) {
										Static.bus.send(new E.Commands.Success("Yay, пост добавлен. ID:" + topic.id));
										Static.bus.send(new E.Commands.Finished());
										Static.bus.send(new E.Commands.Run("post load " + topic.id));

									} else {
										Static.bus.send(new E.Commands.Success("Не удалось создать пост :("));

										Static.bus.send(
												new E.Parts.Run(
														new EditorPart(
																"Пишем пост",
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
							Static.bus.send(new E.Commands.Failure("Не все части поста найдены: " +
									"проверьте, разделены ли теги, текст и заголовок '====='"));
							return false;
						}

					}

					@Override public void cancelled() {
						Static.bus.send(new E.Commands.Finished());
						Static.bus.send(new E.Commands.Clear());
					}
				});

		Static.bus.send(new E.Parts.Run(part, true));
	}

}
