package com.cab404.ponyscape.bus;

import android.content.Intent;
import android.graphics.Bitmap;
import com.cab404.acli.Part;

import java.util.ArrayList;

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

		private Aliases() {}

		public static class Update { }

	}

	public static class Status {
		public final String status;
		public Status(String status) {this.status = status;}
	}

	/**
	 * @author cab404
	 */
	public static class Android {
		private Android() {}

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
		private Commands() {}

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
		 * Запускает крутилку
		 */
		public static class Loading { }

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
		public static class Failure {
			public final CharSequence error;
			public Failure(CharSequence error) {this.error = error;}
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
		private DataRequest() {}

		public static class ListSize {
			public int width, height;
		}
	}

	/**
	 * @author cab404
	 */
	public static class GotData {
		private GotData() {}

		public static class Vote {

			public final int id, votes;
			private Vote(int id, int votes) {
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

		public static class Fav {

			public final int id;
			public final boolean added;
			private Fav(int id, boolean added) {
				this.id = id;
				this.added = added;
			}

			public static class Topic extends Fav {
				public Topic(int id, boolean added) {super(id, added);}
			}

			public static class Comment extends Fav {
				public Comment(int id, boolean added) {super(id, added);}
			}

			public static class Letter extends Fav {
				public Letter(int id, boolean added) {super(id, added);}
			}

		}

		public static class Arch {

			public final int id;
			public final boolean added;
			private Arch(int id, boolean added) {
				this.id = id;
				this.added = added;
			}

			public static class Topic extends Arch {
				public Topic(int id, boolean added) {super(id, added);}
			}

			public static class Letter extends Arch {
				public Letter(int id, boolean added) {super(id, added);}
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
		private Letters() {}


		/**
		 * Массовые действия над письмами ;D
		 */
		public static class SelectAll { }

		public static class CallSelected {
			public ArrayList<Integer> ids = new ArrayList<>();
		}

		public static class UpdateNew {
			public ArrayList<Integer> ids = new ArrayList<>();
		}

		public static class UpdateDeleted {
			public ArrayList<Integer> ids = new ArrayList<>();
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
			public Run(Part part, boolean floating) {
				this.part = part;
				this.floating = floating;
			}
		}

	}
}
