package com.cab404.ponyscape.events;

import com.cab404.acli.Part;

/**
 * @author cab404
 */
public class Parts {
	public static class Add {
		public final Part part;
		public Add(Part part) { this.part = part;}
	}

	public static class Remove {
		public final Part part;
		public Remove(Part part) {this.part = part;}
	}

	/** Очищает список.*/
	public static class Clear {
	}
}
