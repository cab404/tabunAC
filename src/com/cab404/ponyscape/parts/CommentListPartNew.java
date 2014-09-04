package com.cab404.ponyscape.parts;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Comment;
import com.cab404.libtabun.data.Topic;
import com.cab404.libtabun.data.Type;
import com.cab404.libtabun.requests.CommentAddRequest;
import com.cab404.libtabun.requests.CommentEditRequest;
import com.cab404.libtabun.requests.LSRequest;
import com.cab404.libtabun.requests.RefreshCommentsRequest;
import com.cab404.moonlight.util.exceptions.MoonlightFail;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.events.Commands;
import com.cab404.ponyscape.bus.events.Parts;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.images.LevelDrawable;
import com.cab404.ponyscape.utils.views.animation.Anim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cab404
 */
public class CommentListPartNew extends Part {

	/**
	 * Список отступов комментариев.
	 */
	private Map<Integer, Integer> levels;

	private CommentListAdapter adapter;
	/**
	 * Список комментариев
	 */
	private List<Comment> comments;

	/**
	 * Там, где хранятся комментарии
	 */
	private ListView listView;

	/**
	 * Связанный с нашим героем заголовок топика
	 */
	private Topic topic;

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		Static.bus.register(this);

		View view = inflater.inflate(R.layout.part_comment_list, viewGroup, false);
		listView = (ListView) view.findViewById(R.id.comment_list);
		adapter = new CommentListAdapter(context);
		listView.setAdapter(adapter);

		/* Fadein-аем */
		view.setAlpha(0);
		view.animate().alpha(1).setDuration(200);

		return view;
	}

	public CommentListPartNew(Topic topic) {
		this.topic = topic;
		comments = new ArrayList<>();
		levels = new HashMap<>();
	}

	public synchronized void add(Comment comment) {
		// Проверка на заглушку.
		if (comment.deleted) return;

		// Проверка на отсутствие родителя.
		if (comment.parent != 0 && !levels.containsKey(comment.parent)) return;

		// Проверка на повторения.
		for (Comment cm : comments) if (comment.id == cm.id) return;


		if (comment.parent == 0) {
			levels.put(comment.id, 0);
		} else {
			int level = levels.get(comment.parent) + 1;
			levels.put(comment.id, level);

			for (int i = indexOf(comment.parent) + 1; i < comments.size(); i++)
				if (levels.get(comments.get(i).id) < level) {
					comments.add(i, comment);
					return;
				}
		}
		comments.add(comment);
	}

	private int indexOf(int id) {

		for (int i = 0; i < comments.size(); i++)
			if (comments.get(i).id == id)
				return i;

		return -1;
	}

	public void update() {
		if (listView != null)
			((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
	}

	private int max_comment_id() {
		int max = 0;
		for (Comment comment : comments)
			max = Math.max(comment.id, max);
		return max;
	}

	public void refresh() {
		new Thread("Update thread " + topic.id) {
			@Override public void run() {

				RefreshCommentsRequest request =
						new RefreshCommentsRequest(Type.TOPIC, topic.id, max_comment_id());
				try {

					request.exec(Static.user, Static.last_page);

					for (Comment comment : request.comments)
						add(comment);

					Static.handler.post(new Runnable() {
						@Override public void run() {
							update();
						}
					});

				} catch (MoonlightFail f) {
					Static.bus.send(new Commands.Error("Не удалось обновить список комментариев."));
				}
			}
		}.start();
	}

	/**
	 * Запускает комментирование.
	 */
	private void comment(final Comment comment, final boolean isEditing) {
		String[] reply = getContext().getResources().getStringArray(R.array.reply_to);

		String title = isEditing ?
				"Редактируем ошибки"
				:
				(comment == null ?
						"Отвечаем в пост"
						:
						reply[((int) (Math.random() * reply.length))] + comment.author.login);

		EditorPart editorPart =
				new EditorPart(title, "", new EditorPart.EditorActionHandler() {
					@Override public boolean finished(CharSequence text) {
						if (text.length() > 3000 || text.length() < 2) {
							Simple.msg("Текст комментария должен быть от 2 до 3000 символов и не содержать разного рода каку");
							return false;
						}

						final LSRequest request =
								isEditing ?
										new CommentEditRequest(
												comment == null ? 0 : comment.id,
												text.toString()
										)
										:
										new CommentAddRequest(
												Type.BLOG,
												topic.id,
												comment == null ? 0 : comment.id,
												text.toString()
										);

						new Thread(new Runnable() {
							@Override public void run() {
								try {
									request.exec(Static.user, Static.last_page);
									refresh();
								} catch (MoonlightFail f) {
									Static.bus.send(new Commands.Error("Не удалось добавить комментарий."));
								}
							}
						}).start();

						return true;
					}
					@Override public void cancelled() {

					}
				});

		Static.bus.send(new Parts.Run(editorPart));

	}


	/**
	 * Адаптер списка комментариев.
	 */
	private class CommentListAdapter extends BaseAdapter {

		private final int comment_ladder;
		private final Context context;

		private HashMap<Comment, CommentPart> comment_cache;
		private LevelDrawable level_indicator;
		private int c_level = 0;

		public CommentListAdapter(Context context) {
			this.context = context;
			comment_cache = new HashMap<>();
			level_indicator = new LevelDrawable(context.getResources(), 0);
			comment_ladder = context.getResources().getDimensionPixelSize(R.dimen.comment_ladder);
		}

		@Override public int getCount() {
			return comments.size();
		}
		@Override public Object getItem(int i) {
			return comments.get(i);
		}
		@Override public long getItemId(int i) {
			return 0;
		}

		/**
		 * Сдвигает все текущие комментарии на новый уровень.
		 */
		public void setOffset(int offset) {
			if (c_level != offset) {
				c_level = offset;
				Anim.shift(listView, offset * comment_ladder, 100, null);
			}
		}

		@SuppressWarnings({"AssignmentToMethodParameter", "deprecation"})
		@Override public View getView(int i, View view, ViewGroup viewGroup) {
			final Comment comment = comments.get(i);
			CommentPart part;

			/* Проверяем кэш на наличие собранных вьюх */
			if (!comment_cache.containsKey(comment)) {
				part = new CommentPart(comment);
				comment_cache.put(comment, part);
			} else {
				part = comment_cache.get(comment);
			}

			/* Проверяем вьюху на наличие вьюхи */
			if (view == null)
				view = part.create(LayoutInflater.from(viewGroup.getContext()), viewGroup, viewGroup.getContext());
			else
				part.convert(view, viewGroup.getContext());

			/* Начинаем колбаситься */
			LinearLayout.LayoutParams rootLayoutParams = (LinearLayout.LayoutParams) view.findViewById(R.id.root).getLayoutParams();
			final int level = levels.get(comment.id);
			int comment_pixel_offset = levels.get(comment.id) * comment_ladder;

			/* Достаём отступы */
			View right_margin = view.findViewById(R.id.end_clickable_margin);
			View left_margin = view.findViewById(R.id.start_clickable_margin);

			/* Ставим двигалку */
			View.OnClickListener shiftInvoker = new View.OnClickListener() {
				@Override public void onClick(View v) {
					setOffset(level);
				}
			};
			view.findViewById(R.id.data).setOnClickListener(shiftInvoker);
			right_margin.setOnClickListener(shiftInvoker);
			left_margin.setOnClickListener(shiftInvoker);

			/* Достаём размер экрана */
			int dWidth = context.getResources().getDisplayMetrics().widthPixels;
			int dHeight = context.getResources().getDisplayMetrics().heightPixels;

			/* Проставляем отступы */
			left_margin.getLayoutParams().width = comment_pixel_offset;
			right_margin.getLayoutParams().width = c_level * comment_ladder + -comment_pixel_offset;

			/* Ставим размер самого комментария */
			rootLayoutParams.width = Math.min(dWidth, dHeight);

			/* Ставим размер основы вьюхи комментария, на ней сидит и марджин со всем фонами, и root*/
			view.getLayoutParams().width = rootLayoutParams.width + comment_pixel_offset + dWidth;

			/* Ставим цвет всего, что не правый марджин в цвет уровня комментария + 1*/
			view.setBackgroundColor(level_indicator.getLastColor(left_margin.getLayoutParams().width));

			/* Заморочисто ставим фон правого марджина */
			if (Build.VERSION.SDK_INT >= 16) {
				left_margin.setBackground(level_indicator);
			} else {
				left_margin.setBackgroundDrawable(level_indicator);
			}

			/* Ставим слушалки на кнопки */
			view.findViewById(R.id.reply).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					comment(comment, false);
				}
			});
			view.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					comment(comment, true);
				}
			});

			return view;
		}

	}

}
