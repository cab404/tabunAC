package com.cab404.ponyscape.commands;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Int;
import com.cab404.libtabun.data.Comment;
import com.cab404.libtabun.data.Letter;
import com.cab404.libtabun.data.Topic;
import com.cab404.libtabun.util.TabunJSON;
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
import java.util.List;
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
                @Override
                protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
                    View view = super.create(inflater, viewGroup, context);

                    new Thread() {
                        @Override
                        public void run() {
							/* Загружаем данные */
                            final JSONObject post_data = jsonLoad(cached);
                            if (post_data == null) {
                                Static.bus.send(new E.Commands.Finished());
                                return;
                            }

                            if (isLetter) {
									/* Десериализуем заголовок письма*/
                                final Letter header = TabunJSON.parseLetter((JSONObject) post_data.get("header"));
                                Static.handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        add(header);
                                    }
                                });
                            } else {
									/* Десериализуем заголовок поста*/
                                final Topic header = TabunJSON.parseTopic((JSONObject) post_data.get("header"));
                                Static.handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        add(header);
                                    }
                                });
                            }

								/* Десериализуем комментарии, закидывая по 50 за один раз. */
                            ArrayList<Comment> comments = new ArrayList<>();
                            for (Object o : (JSONArray) post_data.get("comments")) {
                                Comment cm = TabunJSON.parseComment((JSONObject) o);
                                comments.add(cm);
                                if (comments.size() > 50) {
                                    final ArrayList<Comment> dump = comments;
                                    comments = new ArrayList<>();
                                    Static.handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (Comment comment : dump)
                                                add(comment);
                                            update();
                                        }
                                    });
                                }
                            }
                            if (!comments.isEmpty()) {
                                final List<Comment> dump = comments;
                                Static.handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (Comment comment : dump)
                                            add(comment);
                                        update();
                                    }
                                });
                            }

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
            @Override
            public void run() {

                final File cache_dir = new File(Static.ctx.getFilesDir(), "saved_posts");
                if (!cache_dir.exists() && !cache_dir.mkdirs()) throw new RuntimeException("Cannot files :(");
                for (File file : cache_dir.listFiles()) {
                    final JSONObject post_data = jsonLoad(file);
                    if (post_data == null) continue;

                    Topic topic = TabunJSON.parseTopic((JSONObject) post_data.get("header"));
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
            @Override
            public void run() {

                final File cache_dir = new File(Static.ctx.getFilesDir(), "saved_letters");
                if (!cache_dir.mkdirs()) throw new RuntimeException("Cannot files :(");
                for (File file : cache_dir.listFiles()) {
                    final JSONObject talk_data = jsonLoad(file);
                    if (talk_data == null) continue;

                    Letter letter = TabunJSON.parseLetter((JSONObject) talk_data.get("header"));
                    LetterPart part = new LetterPart(letter);
                    part.setLink("saved letter " + letter.id);
                    Static.bus.send(new E.Parts.Add(part));
                }

                Static.bus.send(new E.Commands.Finished());
                Static.bus.send(new E.Commands.Clear());
            }
        }.start();

    }


}
