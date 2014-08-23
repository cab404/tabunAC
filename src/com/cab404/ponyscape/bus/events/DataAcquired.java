package com.cab404.ponyscape.bus.events;

import android.graphics.Bitmap;

/**
 * @author cab404
 */
public class DataAcquired {

	public static class PostVote {
		public final int id, votes;
		public PostVote(int id, int votes) {
			this.id = id;
			this.votes = votes;
		}
	}


	public static class CommentVote {
		public final int id, votes;
		public CommentVote(int id, int votes) {
			this.id = id;
			this.votes = votes;
		}
	}

	public static class Image {
		public static class Loaded {
			public final Bitmap loaded;
			public final String src;
			public Loaded(Bitmap loaded, String src) {
				this.loaded = loaded;
				this.src = src;
			}
		}

		public static class Error {
			public final String src;
			public Error(String src) {this.src = src;}
		}
	}

}
