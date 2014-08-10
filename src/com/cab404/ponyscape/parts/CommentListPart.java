package com.cab404.ponyscape.parts;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Comment;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.events.DataRequest;
import com.cab404.ponyscape.bus.events.Parts;
import com.cab404.ponyscape.utils.Anim;
import com.cab404.ponyscape.utils.Static;

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


	@Override protected View create(LayoutInflater inflater, final ViewGroup viewGroup, final Context context) {
		view = (ViewGroup) inflater.inflate(R.layout.part_comment_list, viewGroup, false);
		listView = (ListView) view.findViewById(R.id.comment_list);


		final View expand_view = view.findViewById(R.id.expand_comments);
		final View list_view = view.findViewById(R.id.comment_list_root);

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
								- getContext().getResources().getDimensionPixelSize(R.dimen.list_bottom_padding)
								- getContext().getResources().getDimensionPixelSize(R.dimen.margins),
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

		view.findViewById(R.id.collapse_comments)
				.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View onClick) {
						if (topic_visible) return;
						topic_visible = true;

						Static.bus.send(new Parts.Collapse());

						Anim.fadeIn(expand_view, 200);
						Anim.fadeOut(list_view, 200, new Runnable() {
							@Override public void run() {
								list_view.setVisibility(View.GONE);
								expand_view.setVisibility(View.VISIBLE);
								Anim.resize(
										expand_view,
										saved_height,
										-1,
										200,
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

		return view;
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		super.onRemove(view, parent, context);
		for (CommentPart part : adapter.comment_cache.values()) {
			part.kill();
		}
	}

	private class CommentListAdapter extends BaseAdapter {

		private final Context context;
		private HashMap<View, CommentPart> active_comments;
		private HashMap<Comment, CommentPart> comment_cache;

		public CommentListAdapter(Context context) {
			this.context = context;
			active_comments = new HashMap<>();
			comment_cache = new HashMap<>();
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

		@SuppressWarnings("AssignmentToMethodParameter")
		@Override public View getView(int i, View view, ViewGroup viewGroup) {
			Comment comment = comments.get(i);
			CommentPart part;

			if (!comment_cache.containsKey(comment)) {
				part = new CommentPart(comment);
				comment_cache.put(comment, part);
			} else {
				part = comment_cache.get(comment);
			}

			if (view != null) {
				CommentPart current = active_comments.get(view);
				if (current != null)
					if (current.comment.id != comment.id) {
						Log.v("CommentList",
								"Replaced comment " + current.comment.id + " with " + comment.id + ", "
										+ active_comments.size() + " currently active.");
					}
				active_comments.put(view, part);
			}


			if (view == null)
				view = part.create(LayoutInflater.from(viewGroup.getContext()), viewGroup, viewGroup.getContext());
			else
				part.convert(view, viewGroup.getContext());

			view.getLayoutParams().width = context.getResources().getDisplayMetrics().widthPixels;
			view.setPadding(
					levels.get(comment.id) * context.getResources().getDimensionPixelSize(R.dimen.comment_ladder),
					0, 0, 0
			);
			view.invalidate();
			view.requestLayout();

			return view;
		}
	}
}
