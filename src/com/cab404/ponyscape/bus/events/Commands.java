package com.cab404.ponyscape.bus.events;

/**
 * @author cab404
 */
public class Commands {

	/**
	 * Запускает команду, или добавляет её в очередь выполнения.
	 */
	public static class Run {
		public final String command;
		public Run(String command) {
			this.command = command;
		}
	}

	/**
	 * Снимает блокировку ввода, добавляемую работающей командой.
	 */
	public static class Finished { }

	/**
	 * Очищает бар с командой.
	 */
	public static class Clear { }

	/**
	 * Выводит ошибку на экран.
	 */
	public static class Error {
		public final CharSequence error;
		public Error(CharSequence error) {this.error = error;}
	}

	/**
	 * Заменяет текст звездочками.
	 */
	public static class Hide { }

	/**
	 * Отменяет всё, что только можно, очищает очередь заданий.
	 */
	public static class Abort { }
}
