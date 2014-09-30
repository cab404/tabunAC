package com.cab404.ponyscape.commands;

import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Int;
import com.cab404.libtabun.data.Comment;
import com.cab404.libtabun.pages.LetterPage;
import com.cab404.libtabun.pages.TopicPage;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * @author cab404
 */
@CommandClass(prefix = "save")
public class SaveCommands {

	@Command(command = "post", params = Int.class)
	public void post(final Integer id) {
		new Thread() {
			@SuppressWarnings("unchecked") @Override public void run() {
				try {

					TopicPage page = new TopicPage(id);
					page.fetch(Static.user);

					File directory = new File(Static.ctx.getFilesDir(), "saved_posts");
					if (!directory.exists() && !directory.mkdirs()) throw new RuntimeException();
					File save = new File(directory, id + ".json.gz");

					JSONObject data = new JSONObject();
					JSONArray comments = new JSONArray();

					data.put("comments", comments);
					if (page.header == null) {
						Static.bus.send(new E.Commands.Error("Не удалось сохранить пост - для нас его нет."));
						Static.bus.send(new E.Commands.Finished());
						return;
					}
					data.put("header", page.header.toJSON());
					for (Comment c : page.comments)
						comments.add(c.toJSON());

					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(save))));
					data.writeJSONString(writer);
					writer.close();

					Static.bus.send(new E.GotData.Arch.Topic(id, true));
					Static.bus.send(new E.Commands.Success("Пост сохранён."));
					Static.bus.send(new E.Commands.Finished());
					Static.bus.send(new E.Commands.Clear());

				} catch (IOException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				super.run();
			}
		}.start();
	}


	@Command(command = "letter", params = Int.class)
	public void letter(final Integer id) {
		new Thread() {
			@SuppressWarnings("unchecked") @Override public void run() {
				try {

					LetterPage page = new LetterPage(id);
					page.fetch(Static.user);

					File directory = new File(Static.ctx.getFilesDir(), "saved_letters");
					if (!directory.exists() && !directory.mkdirs()) throw new RuntimeException();
					File save = new File(directory, id + ".json.gz");

					JSONObject data = new JSONObject();
					JSONArray comments = new JSONArray();

					data.put("comments", comments);
					if (page.header == null) {
						Static.bus.send(new E.Commands.Error("Не удалось сохранить письмо, в нашем измерении его не существует."));
						Static.bus.send(new E.Commands.Finished());
						return;
					}
					data.put("header", page.header.toJSON());
					for (Comment c : page.comments)
						comments.add(c.toJSON());

					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(save))));
					data.writeJSONString(writer);
					writer.close();

					Static.bus.send(new E.GotData.Arch.Letter(id, true));
					Static.bus.send(new E.Commands.Success("Письмо сохранёно."));
					Static.bus.send(new E.Commands.Finished());
					Static.bus.send(new E.Commands.Clear());

				} catch (IOException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				super.run();
			}
		}.start();
	}


}
