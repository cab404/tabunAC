package com.cab404.ponyscape.utils.state;

import android.util.Log;
import com.cab404.libtabun.util.JSONable;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author cab404
 */
public class ArchiveUtils {

    public static final String
            POSTS_DIR = "saved_posts",
            MAILS_DIR = "saved_letters";


    public static boolean isLetterInArchive(int id) {
        final File cache_dir = new File(Static.ctx.getFilesDir(), MAILS_DIR);
        final File cached = new File(cache_dir, id + ".json.gz");
        return cached.exists();
    }


    public static boolean isPostInArchive(int id) {
        final File cache_dir = new File(Static.ctx.getFilesDir(), POSTS_DIR);
        final File cached = new File(cache_dir, id + ".json.gz");
        return cached.exists();
    }

    public static void deleteLetter(int id) {
        final File cache_dir = new File(Static.ctx.getFilesDir(), MAILS_DIR);
        final File cached = new File(cache_dir, id + ".json.gz");
        if (cached.delete())
            Static.bus.send(new E.GotData.Arch.Letter(id, false));
    }


    public static void deletePost(int id) {
        final File cache_dir = new File(Static.ctx.getFilesDir(), POSTS_DIR);
        final File cached = new File(cache_dir, id + ".json.gz");
        if (cached.delete())
            Static.bus.send(new E.GotData.Arch.Topic(id, false));
    }


    public static JSONObject jsonLoad(File file) {
        try {
            return (JSONObject)
                    new JSONParser().parse(
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

    public static Save savePost(int id) {
        final File cache_dir = new File(Static.ctx.getFilesDir(), POSTS_DIR);
        return new Save(new File(cache_dir, id + ".json.gz"));
    }


    public static Save saveLetter(int id) {
        final File cache_dir = new File(Static.ctx.getFilesDir(), MAILS_DIR);
        return new Save(new File(cache_dir, id + ".json.gz"));
    }

    public static class Save {

        private File where;
        private JSONObject data;

        private Save(File where) {
            this.where = where;
            data = new JSONObject();
        }

        @SuppressWarnings("unchecked")
        public void setHeader(JSONable header) {
            try {
                data.put("header", header.toJSON());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        public void addComment(JSONable comment) {
            try {
                JSONArray comments = (JSONArray) data.get("comments");

                if (comments == null) {
                    comments = new JSONArray();
                    data.put("comments", comments);
                }

                comments.add(comment.toJSON());

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public void write() {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(
                        new OutputStreamWriter(
                                new GZIPOutputStream(
                                        new FileOutputStream(
                                                where
                                        )
                                ),
                                "UTF-8"
                        )
                );
                data.writeJSONString(writer);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (writer != null)
                    try {
                        writer.close();
                    } catch (IOException ex) {
                        Log.wtf("ArchiveFileWriter", "Cannot close file");
                    }
            }

        }

    }

}

