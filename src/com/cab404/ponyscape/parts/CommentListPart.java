package com.cab404.ponyscape.parts;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Comment;
import com.cab404.ponyscape.R;
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

	boolean topic_visible = true;
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
		((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
	}

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, final Context context) {
		View view = inflater.inflate(R.layout.part_comment_list, viewGroup, false);
		listView = (ListView) view.findViewById(R.id.comment_list);
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

		listView.getLayoutParams().height =
				displayMetrics.heightPixels -
						context.getResources()
								.getDimensionPixelSize(R.dimen.list_bottom_padding);

		view.findViewById(R.id.favourite).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {

				Static.handler.post(new Runnable() {
					@Override public void run() {
						if (!(topic_visible = !topic_visible))
							topicPart.delete();
						else
							insertBefore(topicPart);
					}
				});
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
