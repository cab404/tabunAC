package com.cab404.ponyscape.parts;

import android.animation.Animator;
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
import com.cab404.ponyscape.bus.events.DataRequest;
import com.cab404.ponyscape.bus.events.Parts;
import com.cab404.ponyscape.utils.Anim;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.images.LevelDrawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Когда с постами ещё можно обойтись ListView, тут памяти просто не хватит. Так что адаптеры.
 *
 * @author cab404
 */
public class CommentListPart extends Part {
	private CommentListAdapter adapter;
	private Map<Integer, Integer> levels;
	private List<Comment> comments;
	private ListView listView;
	private ViewGroup view;

	boolean topic_visible = true;
	int saved_height = 0;
	private TopicPart topicPart;


	public CommentListPart(TopicPart topicPart) {
		this.topicPart = topicPart;
		comments = new ArrayList<>();
		levels = new HashMap<>();
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

	public void comment(final Comment comment, final boolean isEditing) {
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

//				if (isEditing) {
//
//					new CommentEditRequest(comment.id, text.toString());
//
//				}

				return false;
			}
			@Override public void cancelled() {

			}
		});

		Runnable runnable = new Runnable() {
			@Override public void run() {

			}
		};
	}

	@Override protected View create(LayoutInflater inflater, final ViewGroup viewGroup, final Context context) {
		view = (ViewGroup) inflater.inflate(R.layout.part_comment_list, viewGroup, false);
		listView = (ListView) view.findViewById(R.id.comment_list);


		final View expand_view = view.findViewById(R.id.expand_comments);
		final View list_view = view.findViewById(R.id.comment_list_root);

		/* Настраиваем раскрытие дерева комментариев */
		expand_view.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View onClick) {
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
						heightPixels
//								- getContext().getResources().getDimensionPixelSize(R.dimen.list_bottom_padding)
//								- getContext().getResources().getDimensionPixelSize(R.dimen.margins)
						,
						-1,
						200,
						new Runnable() {
							@Override public void run() {
								list_view.setVisibility(View.VISIBLE);
								expand_view.setVisibility(View.GONE);

								list_view.getLayoutParams().height = expand_view.getLayoutParams().height;
								view.requestLayout();

								Anim.fadeIn(list_view, 200, new Runnable() {
									@Override public void run() {
										Log.v("CommentListPart", "Expand finished.");
									}
								});
							}
						}
				);

			}
		});

		/* Скрытие дерева комментариев */
		view.findViewById(R.id.collapse_comments)
				.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View onClick) {
						if (topic_visible) return;
						topic_visible = true;

						/* Включаем обратно скролл и комманд-бар*/
						Static.bus.send(new Parts.Collapse());

						/* Показываем вьюху раскрытия */
						Anim.fadeIn(expand_view, 200);
						/* Выносим прозрачность у дерева, чтобы потом убить его в ноль и менять высоту одного expand_view */
						Anim.fadeOut(list_view, 100, new Runnable() {
							@Override public void run() {
								list_view.setVisibility(View.GONE);
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
		if (!topic_visible)
			Static.bus.send(new Parts.Collapse());

		for (CommentPart part : adapter.comment_cache.values()) {
			part.kill();
		}
	}

	/**
	 * Отступ всего дерева проставляем через setX на каждом комментарии,
	 * для отступов уровня используем отдельный View в комментарии,
	 * в него заодно пихаем слушалки перехода по уровням и фоны.
	 */
	private class CommentListAdapter extends BaseAdapter {


		private HashMap<Comment, CommentPart> comment_cache;
		private final Context context;

		private LevelDrawable level_indicator;
		private int c_level = 0;
		private final int comment_ladder;

		private boolean animation_running = false;
		private View commitee = null;

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

				int comment_pixel_offset = offset * comment_ladder;

				for (int i = 0; i < listView.getChildCount(); i++) {
					View child = listView.getChildAt(i);
					/* Откуда будем брать новый X-offset */
					animation_running = true;
					commitee = child;
					child.animate()
							.x(-comment_ladder * c_level)
							.setListener(new Anim.AnimatorListenerImpl() {
								@Override public void onAnimationEnd(Animator animation) {
									animation.removeAllListeners();

								}
							});
				}

			}
		}

		private float getCurrentPixelOffset() {
			if (animation_running) {
				return commitee.getX();
			} else
				return -comment_ladder * c_level;
		}

		@SuppressWarnings("AssignmentToMethodParameter")
		@Override public View getView(int i, View view, ViewGroup viewGroup) {
			final Comment comment = comments.get(i);
			CommentPart part;

			if (!comment_cache.containsKey(comment)) {
				part = new CommentPart(comment);
				comment_cache.put(comment, part);
			} else {
				part = comment_cache.get(comment);
			}

			if (view == null)
				view = part.create(LayoutInflater.from(viewGroup.getContext()), viewGroup, viewGroup.getContext());
			else
				part.convert(view, viewGroup.getContext());

			final int level = levels.get(comment.id);

			LinearLayout.LayoutParams rootLayoutParams = (LinearLayout.LayoutParams) view.findViewById(R.id.root).getLayoutParams();
			int comment_pixel_offset = levels.get(comment.id) * comment_ladder;

			view.setX(getCurrentPixelOffset());

			View.OnClickListener shiftInvoker = new View.OnClickListener() {
				@Override public void onClick(View v) {
					setOffset(level);
				}
			};

			View right_margin = view.findViewById(R.id.end_clickable_margin);
			View left_margin = view.findViewById(R.id.start_clickable_margin);

			left_margin.setOnClickListener(shiftInvoker);
			right_margin.setOnClickListener(shiftInvoker);

			int dWidth = context.getResources().getDisplayMetrics().widthPixels;
			int dHeight = context.getResources().getDisplayMetrics().heightPixels;

			view.findViewById(R.id.reply).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					comment(comment, false);
				}
			});

			left_margin.getLayoutParams().width = comment_pixel_offset;
			right_margin.getLayoutParams().width = dWidth;

			rootLayoutParams.width = Math.min(dWidth, dHeight);

			view.getLayoutParams().width = rootLayoutParams.width + comment_pixel_offset + dWidth;

			view.setBackgroundColor(level_indicator.getLastColor(left_margin.getLayoutParams().width));

			if (Build.VERSION.SDK_INT >= 16) {
				left_margin.setBackground(level_indicator);
			} else {
				left_margin.setBackgroundDrawable(level_indicator);
			}


			return view;
		}
	}
}
