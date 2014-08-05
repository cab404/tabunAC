package com.cab404.ponyscape.bus.events;

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

	public static class Focus {
		public final Part part;
		public Focus(Part part) {this.part = part;}
	}

	/**
	 * Убирает padding внизу листа
	 */
	public static class Expand { }

	/**
	 * Возвращает padding внизу листа
	 */
	public static class Collapse { }

	/**
	 * Очищает список.
	 */
	public static class Clear { }

	/**
	 * Блокирует прокрутку списка
	 */
	public static class Lock { }

	/**
	 * Разлокирует прокрутку списка
	 */
	public static class Unlock { }

	public static class Hide {
		public final Part part;
		public Hide(Part part) {this.part = part;}
	}

	public static class Show {
		public final Part part;
		public Show(Part part) {this.part = part;}
	}
}
