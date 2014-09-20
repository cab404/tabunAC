package com.cab404.ponyscape.bus;

import android.content.Intent;
import android.graphics.Bitmap;
import com.cab404.acli.Part;

/**
 * E is for events!
 *
 * @author cab404
 */
public class E {

	/**
	 * @author cab404
	 */
	public static class Aliases {

		public static class Update { }

	}

	/**
	 * @author cab404
	 */
	public static class Android {

		public static class RootSizeChanged { }

		/**
		 * Запускает задание из текущей Activity
		 */
		public static class StartActivityForResult {
			public static interface ResultHandler {
				public void handle(int resultCode, Intent intent);
				public void error(Throwable e);
			}

			public final Intent intent;
			public final ResultHandler handler;
			public StartActivityForResult(Intent intent, ResultHandler handler) {
				this.intent = intent;
				this.handler = handler;
			}
		}

		public static class StartActivity {
			public final Intent activity;
			public StartActivity(Intent activity) {this.activity = activity;}
		}

	}

	/**
	 * @author cab404
	 */
	public static class Commands {

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
		 * Выводит ня на экран.
		 */
		public static class Success {
			public final CharSequence msg;
			public Success(CharSequence msg) {this.msg = msg;}
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

	/**
	 * @author cab404
	 */
	public static class DataRequest {
		public static class ListSize {
			public int width, height;
		}
	}

	/**
	 * @author cab404
	 */
	public static class GotData {

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

	/**
	 * @author cab404
	 */
	public static class Letters {

		/**
		 * Массовые действия над письмами ;D
		 */
		public enum MassEffect {
			DELETE, READ, SELECT
		}

	}

	/**
	 * @author cab404
	 */
	public static class Login {
		public static class Requested { }

		public static class Success { }

		public static class Failure { }
	}

	/**
	 * @author cab404
	 */
	public static class Parts {
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
		 * Разблокирует прокрутку списка
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

		/**
		 * Запускает часть в отдельной Activity
		 */
		public static class Run {
			public final Part part;
			public final boolean floating;
			public Run(Part part, boolean floating) {this.part = part;
				this.floating = floating;
			}
		}

	}
}
