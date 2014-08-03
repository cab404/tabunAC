package com.cab404.ponyscape.parts;

import android.content.Context;
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
 * @author cab404
 */
public class CommentListPart extends Part {
	private List<Comment> comments;
	private Map<Integer, Integer> levels;
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

		DataRequest.ListSize height = new DataRequest.ListSize();
		Static.bus.send(height);
		final int heightPixels = height.height;

		view.findViewById(R.id.expand_comments)
				.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View onClick) {
						if (!topic_visible) return;
						topic_visible = false;

						topicPart.hide();

						Anim.fadeOut(view.findViewById(R.id.expand_comments));

						Anim.resize(
								view,
								heightPixels
										- getContext().getResources().getDimensionPixelSize(R.dimen.list_bottom_padding)
										- getContext().getResources().getDimensionPixelSize(R.dimen.margins) * 2,
								-1,
								200,
								new Runnable() {
									@Override public void run() {

									}
								}
						);
						saved_height = view.getHeight();
						Static.bus.send(new Parts.Expand());
					}
				});
		view.findViewById(R.id.collapse_comments)
				.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View onClick) {
						if (topic_visible) return;
						topic_visible = true;

						topicPart.show();

						Anim.fadeIn(view.findViewById(R.id.expand_comments));

						Anim.resize(
								view,
								saved_height,
								-1,
								200,
								new Runnable() {
									@Override public void run() {
										topic_visible = true;
									}
								}
						);
						Static.bus.send(new Parts.Collapse());
					}
				});


		listView.setAdapter(new BaseAdapter() {
			@Override public int getCount() {
				return comments.size();
			}
			@Override public Object getItem(int i) {
				return comments.get(i);
			}
			@Override public long getItemId(int i) {
				return comments.get(i).hashCode();
			}

			@SuppressWarnings("AssignmentToMethodParameter")
			@Override public View getView(int i, View view, ViewGroup viewGroup) {
				CommentPart part = new CommentPart(comments.get(i));

				if (view == null)
					view = part.create(LayoutInflater.from(viewGroup.getContext()), viewGroup, viewGroup.getContext());
				else
					part.convert(view, viewGroup.getContext());

				view.getLayoutParams().width = context.getResources().getDisplayMetrics().widthPixels;
				view.setPadding(
						levels.get(comments.get(i).id) * context.getResources().getDimensionPixelSize(R.dimen.comment_ladder),
						0, 0, 0
				);
				view.invalidate();
				view.requestLayout();

				return view;
			}
		});

		return view;
	}

}
