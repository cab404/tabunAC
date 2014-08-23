package com.cab404.ponyscape.parts;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Comment;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.AppContextExecutor;
import com.cab404.ponyscape.bus.events.Android;
import com.cab404.ponyscape.bus.events.DataRequest;
import com.cab404.ponyscape.bus.events.Parts;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.images.LevelDrawable;
import com.cab404.ponyscape.utils.views.animation.Anim;
import com.cab404.sjbus.Bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Когда с постами ещё можно обойтись LinearLayout, тут памяти просто не хватит. Так что адаптеры.
 *
 * @author cab404
 */
public class CommentListPart extends Part {
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
	 * Root-батюшка
	 */
	private ViewGroup view;

	/**
	 * Не раскрыто ли дерево комментариев?
	 */
	private boolean topic_visible = true;

	/**
	 * Сохранённая высота блямбы раскрытия комментариев. Криво. Но работает. (Но криво)
	 */
	private int saved_height = 0;

	/**
	 * Связанный с нашим героем заголовок топика
	 */
	private TopicPart topicPart;

	/**
	 * Блямба, на которую если нажать, то появятся комментарии.
	 */
	private View expand_view;

	/**
	 * То, в чем лежат кнопки управления и пост.
	 */
	private View list_root;


	public CommentListPart(TopicPart topicPart) {
		this.topicPart = topicPart;
		comments = new ArrayList<>();
		levels = new HashMap<>();
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void onConfigChange(Android.RootSizeChanged e) {
		if (!topic_visible) {
			listView.invalidate();
			DataRequest.ListSize size = new DataRequest.ListSize();
			Static.bus.send(size);
			list_root.getLayoutParams().height = expand_view.getLayoutParams().height = size.height;
			view.requestLayout();
		}

	}

	public void add(Comment comment) {
		if (comment.deleted) return;
		if (comment.parent != 0 && !levels.containsKey(comment.parent)) return;
		if (comment.parent == 0)
			levels.put(comment.id, 0);
		else
			levels.put(comment.id, levels.get(comment.parent) + 1);

		comments.add(comment);
	}

	public void update() {
		if (listView != null)
			((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
	}

	private void comment(final Comment comment, final boolean isEditing) {
		String[] reply = getContext().getResources().getStringArray(R.array.reply_to);
		String title = comment == null ?
				"Отвечаем в пост"
				:
				reply[((int) (Math.random() * reply.length))] + comment.author;
		new EditorPart(title, "", new EditorPart.EditorActionHandler() {
			@Override public boolean finished(CharSequence text) {
				if (text.length() > 3000 || text.length() < 2) {
					Simple.msg("Текст комментария должен быть от 2 до 3000 символов и не содержать разного рода каку");
					return false;
				}

				return false;
			}
			@Override public void cancelled() {}
		});

		Runnable runnable = new Runnable() {
			@Override public void run() {

			}
		};
	}

	private void hideTree() {
		if (topic_visible) return;
		topic_visible = true;

						/* Включаем обратно скролл и комманд-бар*/
		Static.bus.send(new Parts.Collapse());

						/* Показываем вьюху раскрытия */
		Anim.fadeIn(expand_view, 200);
						/* Выносим прозрачность у дерева, чтобы потом убить его в ноль и менять высоту одного expand_view */
		Anim.fadeOut(list_root, 100, new Runnable() {
			@Override public void run() {
				list_root.setVisibility(View.GONE);
				expand_view.setVisibility(View.VISIBLE);
				Anim.resize(
						expand_view,
						saved_height,
						-1,
						100,
						new Runnable() {
							@Override public void run() {
								topic_visible = true;
								topicPart.show();
							}
						}
				);

			}
		});

	}

	private void showTree() {
		if (!topic_visible) return;
		topic_visible = false;

		DataRequest.ListSize height = new DataRequest.ListSize();
		Static.bus.send(height);
		final int heightPixels = height.height;

		topicPart.hide();
		saved_height = expand_view.getHeight();
		Static.bus.send(new Parts.Expand());

		Anim.fadeOut(expand_view, 200);
		Anim.resize(
				expand_view,
				heightPixels,
				-1,
				200,
				new Runnable() {
					@Override public void run() {
						list_root.setVisibility(View.VISIBLE);
						expand_view.setVisibility(View.GONE);

						list_root.getLayoutParams().height = expand_view.getLayoutParams().height;
						view.requestLayout();

						Anim.fadeIn(list_root, 200, new Runnable() {
							@Override public void run() {
								Log.v("CommentListPart", "Expand finished.");
							}
						});
					}
				}
		);
	}

	@Override protected View create(LayoutInflater inflater, final ViewGroup viewGroup, final Context context) {
		Static.bus.register(this);
		view = (ViewGroup) inflater.inflate(R.layout.part_comment_list, viewGroup, false);
		listView = (ListView) view.findViewById(R.id.comment_list);

		expand_view = view.findViewById(R.id.expand_comments);
		list_root = view.findViewById(R.id.comment_list_root);

		/* Раскрытие дерева комментариев */
		expand_view.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View onClick) {
				showTree();
			}
		});

		/* Скрытие дерева комментариев */
		view.findViewById(R.id.collapse_comments).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View onClick) {
				hideTree();
			}
		});

		adapter = new CommentListAdapter(context);
		listView.setAdapter(adapter);

		/* Fadein-аем */
		view.setAlpha(0);
		view.animate().alpha(1).setDuration(200);

		return view;
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		super.onRemove(view, parent, context);
		Static.bus.unregister(this);
		if (!topic_visible)
			Static.bus.send(new Parts.Collapse());

		for (CommentPart part : adapter.comment_cache.values()) {
			part.kill();
		}
	}

	/**
	 * Отступ всего дерева проставляем через setX в listView,
	 * для отступов уровня используем отдельный View в комментарии,
	 * в него заодно пихаем слушалки перехода по уровням и фоны.
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

		@SuppressWarnings("AssignmentToMethodParameter")
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
			left_margin.setOnClickListener(shiftInvoker);
			right_margin.setOnClickListener(shiftInvoker);

			/* Достаём размер экрана */
			int dWidth = context.getResources().getDisplayMetrics().widthPixels;
			int dHeight = context.getResources().getDisplayMetrics().heightPixels;

			/* Проставляем отступы */
			left_margin.getLayoutParams().width = comment_pixel_offset;
			right_margin.getLayoutParams().width = dWidth;

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

			return view;
		}
	}
}
