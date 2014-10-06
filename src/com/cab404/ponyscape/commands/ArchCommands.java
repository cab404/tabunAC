package com.cab404.ponyscape.commands;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Int;
import com.cab404.libtabun.data.*;
import com.cab404.libtabun.util.Tabun;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.parts.CommentListPart;
import com.cab404.ponyscape.parts.LetterPart;
import com.cab404.ponyscape.parts.TopicPart;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.state.ArchiveUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 * Архив
 *
 * @author cab404
 */
@CommandClass(prefix = "saved")
public class ArchCommands {

	/**
	 * Загружает пост из архива.
	 */
	JSONParser parser = new JSONParser();
	private JSONObject jsonLoad(File file) {
		try {
			return (JSONObject)
					parser.parse(
							new BufferedReader(
									new InputStreamReader(
											new GZIPInputStream(
													new FileInputStream(file)
											)
									)
							)
					);
		} catch (IOException | ParseException e) {
			Static.bus.send(new E.Commands.Failure("Ошибка при загрузке данных из архива: " + file));
			Log.w("ERR", e);
			if (!file.delete())
				Log.wtf("Archive", "Не могу удалить запись из архива: " + file);
			return null;
		}
	}

	private void load(int id, final boolean isLetter) {

		final File cache_dir = new File(Static.ctx.getFilesDir(), "saved_" + (isLetter ? "letters" : "posts"));
		final File cached = new File(cache_dir, id + ".json.gz");

		if (cached.exists()) {
	 	    /* Если файл есть, то создаём сразу лист. */
			CommentListPart part = new CommentListPart(id, isLetter) {
				@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
					View view = super.create(inflater, viewGroup, context);

					new Thread() {
						@Override public void run() {
								/* Загружаем данные */
							JSONParser parser = new JSONParser();
							final JSONObject post_data = jsonLoad(cached);
							if (post_data == null) {
								Static.bus.send(new E.Commands.Finished());
								return;
							}

							if (isLetter) {
									/* Десериализуем заголовок письма*/
								final Letter header = parseLetter((JSONObject) post_data.get("header"));
								Static.handler.post(new Runnable() {
									@Override public void run() {add(header);}
								});
							} else {
									/* Десериализуем заголовок поста*/
								final Topic header = parseTopic((JSONObject) post_data.get("header"));
								Static.handler.post(new Runnable() {
									@Override public void run() {add(header);}
								});
							}

								/* Десериализуем комментарии, закидывая по 50 за один раз. */
							ArrayList<Comment> comments = new ArrayList<>();
							for (Object o : (JSONArray) post_data.get("comments")) {
								Comment cm = parseComment((JSONObject) o);
								comments.add(cm);
								if (comments.size() > 50) {
									final ArrayList<Comment> dump = comments;
									comments = new ArrayList<>();
									Static.handler.post(new Runnable() {
										@Override public void run() {
											while (!dump.isEmpty())
												add(dump.remove(0));
											update();
										}
									});
								}
							}
							while (!comments.isEmpty())
								add(comments.remove(0));

							Static.bus.send(new E.Commands.Finished());
							Static.bus.send(new E.Commands.Clear());

						}
					}
							.
									start();

					return view;
				}
			};
			Static.bus.send(new E.Parts.Run(part, false));

		} else {

			Static.bus.send(new E.Commands.Failure("Нет такой записи в архиве."));

		}


	}

	@Command(command = "post", params = Int.class)
	public void loadPostFromCache(Integer id) {
		load(id, false);
	}

	@Command(command = "letter", params = Int.class)
	public void loadLetterFromCache(Integer id) {
		load(id, true);
	}

	@Command(command = "delete_post", params = Int.class)
	public void deletePostFromCache(Integer id) {
		ArchiveUtils.deletePost(id);
		Static.bus.send(new E.GotData.Arch.Topic(id, false));
		Static.bus.send(new E.Commands.Success("Пост удалён из архива."));
		Static.bus.send(new E.Commands.Finished());
		Static.bus.send(new E.Commands.Clear());

	}

	@Command(command = "delete_letter", params = Int.class)
	public void deleteLetterFromCache(Integer id) {
		ArchiveUtils.deleteLetter(id);
		Static.bus.send(new E.GotData.Arch.Letter(id, false));
		Static.bus.send(new E.Commands.Success("Письмо удалено из архива."));
		Static.bus.send(new E.Commands.Finished());
		Static.bus.send(new E.Commands.Clear());
	}


	@Command(command = "posts")
	public void posts() {
		Static.bus.send(new E.Parts.Clear());
		new Thread() {
			@Override public void run() {

				final File cache_dir = new File(Static.ctx.getFilesDir(), "saved_posts");

				for (File file : cache_dir.listFiles()) {
					final JSONObject post_data = jsonLoad(file);
					if (post_data == null) continue;

					Topic topic = parseTopic((JSONObject) post_data.get("header"));
					TopicPart part = new TopicPart(topic);
					part.setLink("saved post " + topic.id);
					Static.bus.send(new E.Parts.Add(part));
				}

				Static.bus.send(new E.Commands.Finished());
				Static.bus.send(new E.Commands.Clear());
			}
		}.start();

	}


	@Command(command = "letters")
	public void letters() {
		Static.bus.send(new E.Parts.Clear());
		new Thread() {
			@Override public void run() {

				final File cache_dir = new File(Static.ctx.getFilesDir(), "saved_letters");

				for (File file : cache_dir.listFiles()) {
					final JSONObject talk_data = jsonLoad(file);
					if (talk_data == null) continue;

					Letter letter = parseLetter((JSONObject) talk_data.get("header"));
					LetterPart part = new LetterPart(letter);
					part.setLink("saved letter " + letter.id);
					Static.bus.send(new E.Parts.Add(part));
				}

				Static.bus.send(new E.Commands.Finished());
				Static.bus.send(new E.Commands.Clear());
			}
		}.start();

	}


	private static Blog parseBlog(JSONObject object) {
		if (object == null) return null;

		Blog blog = new Blog();
		if (object.containsKey("name")) blog.name = (String) object.get("name");
		if (object.containsKey("icon")) blog.icon = (String) object.get("icon");
		if (object.containsKey("about")) blog.about = (String) object.get("about");
		if (object.containsKey("rating")) blog.rating = ((Double) object.get("rating")).floatValue();
		if (object.containsKey("readers")) blog.readers = ((Long) object.get("readers")).intValue();
		if (object.containsKey("creation_date"))
			blog.creation_date = Tabun.parseSQLDate((String) object.get("creation_date"));
		if (object.containsKey("url_name")) blog.url_name = (String) object.get("url_name");
		if (object.containsKey("restricted")) blog.restricted = (boolean) object.get("restricted");

		return blog;
	}

	private static Profile parseProfile(JSONObject object) {
		if (object == null) return null;

		Profile profile = new Profile();
		if (object.containsKey("small_icon")) profile.small_icon = (String) object.get("small_icon");
		if (object.containsKey("mid_icon")) profile.mid_icon = (String) object.get("mid_icon");
		if (object.containsKey("big_icon")) profile.big_icon = (String) object.get("big_icon");
		if (object.containsKey("strength")) profile.strength = ((Double) object.get("strength")).floatValue();
		if (object.containsKey("login")) profile.login = (String) object.get("login");
		if (object.containsKey("about")) profile.about = (String) object.get("about");
		if (object.containsKey("photo")) profile.photo = (String) object.get("photo");
		if (object.containsKey("votes")) profile.votes = ((Double) object.get("votes")).floatValue();
		if (object.containsKey("name")) profile.name = (String) object.get("name");
		if (object.containsKey("id")) profile.id = ((Long) object.get("id")).intValue();

		return profile;
	}

	private static Topic parseTopic(JSONObject object) {
		if (object == null) return null;

		Topic topic = new Topic();
		if (object.containsKey("id")) topic.id = ((Long) object.get("id")).intValue();
		if (object.containsKey("date")) topic.date = Tabun.parseSQLDate((String) object.get("date"));
		if (object.containsKey("text")) topic.text = (String) object.get("text");
		if (object.containsKey("title")) topic.title = (String) object.get("title");
		if (object.containsKey("votes")) topic.votes = (String) object.get("votes");
		if (object.containsKey("comments")) topic.comments = ((Long) object.get("comments")).intValue();
		if (object.containsKey("your_vote")) topic.your_vote = ((Long) object.get("your_vote")).intValue();
		if (object.containsKey("blog")) topic.blog = parseBlog((JSONObject) object.get("blog"));
		if (object.containsKey("comments_new")) topic.comments_new = ((Long) object.get("comments_new")).intValue();
		if (object.containsKey("author")) topic.author = parseProfile((JSONObject) object.get("author"));
		if (object.containsKey("vote_enabled")) topic.vote_enabled = (boolean) object.get("vote_enabled");
		if (object.containsKey("in_favourites")) topic.in_favourites = (boolean) object.get("in_favourites");


		if (object.containsKey("tags")) {
			topic.tags = new ArrayList<>();
			for (Object obj : (JSONArray) object.get("tags"))
				topic.tags.add((String) obj);
		}

		return topic;
	}

	private static Comment parseComment(JSONObject object) {
		if (object == null) return null;
		Comment comment = new Comment();

		if (object.containsKey("author")) comment.author = parseProfile((JSONObject) object.get("author"));
		if (object.containsKey("date")) comment.date = Tabun.parseSQLDate((String) object.get("date"));
		if (object.containsKey("deleted")) comment.deleted = (boolean) object.get("deleted");
		if (object.containsKey("in_favs")) comment.in_favs = (boolean) object.get("in_favs");
		if (object.containsKey("is_new")) comment.is_new = (boolean) object.get("is_new");
		if (object.containsKey("parent")) comment.parent = ((Long) object.get("parent")).intValue();
		if (object.containsKey("text")) comment.text = (String) object.get("text");
		if (object.containsKey("votes")) comment.votes = ((Long) object.get("votes")).intValue();
		if (object.containsKey("id")) comment.id = ((Long) object.get("id")).intValue();

		return comment;
	}

	private static Letter parseLetter(JSONObject object) {
		if (object == null) return null;

		Letter letter = new Letter();
		if (object.containsKey("id")) letter.id = ((Long) object.get("id")).intValue();
		if (object.containsKey("date")) letter.date = Tabun.parseSQLDate((String) object.get("date"));
		if (object.containsKey("text")) letter.text = (String) object.get("text");
		if (object.containsKey("title")) letter.title = (String) object.get("title");
		if (object.containsKey("starter")) letter.starter = parseProfile((JSONObject) object.get("starter"));
		if (object.containsKey("comments")) letter.comments = ((Long) object.get("comments")).intValue();
		if (object.containsKey("comments_new")) letter.comments_new = ((Long) object.get("comments_new")).intValue();

		if (object.containsKey("recipients")) {
			letter.recipients = new ArrayList<>();
			for (Object obj : (JSONArray) object.get("recipients"))
				letter.recipients.add((String) obj);
		}

		return letter;
	}

}
