package com.cab404.ponyscape.utils.state;

import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;

import java.io.File;

/**
 * @author cab404
 */
public class ArchiveUtils {

	public static boolean isLetterInArchive(int id) {
		final File cache_dir = new File(Static.ctx.getFilesDir(), "saved_letters");
		final File cached = new File(cache_dir, id + ".json.gz");
		return cached.exists();
	}


	public static boolean isPostInArchive(int id) {
		final File cache_dir = new File(Static.ctx.getFilesDir(), "saved_posts");
		final File cached = new File(cache_dir, id + ".json.gz");
		return cached.exists();
	}

	public static void deleteLetter(int id) {
		final File cache_dir = new File(Static.ctx.getFilesDir(), "saved_letters");
		final File cached = new File(cache_dir, id + ".json.gz");
		if (cached.delete())
			Static.bus.send(new E.GotData.Arch.Letter(id, false));
	}


	public static void deletePost(int id) {
		final File cache_dir = new File(Static.ctx.getFilesDir(), "saved_posts");
		final File cached = new File(cache_dir, id + ".json.gz");
		if (cached.delete())
			Static.bus.send(new E.GotData.Arch.Topic(id, false));
	}


}
