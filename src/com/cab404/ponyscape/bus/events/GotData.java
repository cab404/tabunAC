package com.cab404.ponyscape.bus.events;

import android.graphics.Bitmap;

/**
 * @author cab404
 */
public class GotData {

	public static class Vote {

		public final int id, votes;
		public Vote(int id, int votes) {
			this.id = id;
			this.votes = votes;
		}

		public static class Topic extends Vote {
			public Topic(int id, int votes) {super(id, votes);}
		}

		public static class Comment extends Vote {
			public Comment(int id, int votes) {super(id, votes);}
		}

		public static class Blog extends Vote {
			public final float votes;
			public Blog(int id, float votes) {
				super(id, 0);
				this.votes = votes;
			}
		}

		public static class User extends Blog {
			public User(int id, float votes) {super(id, votes);}
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
