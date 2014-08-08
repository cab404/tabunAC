package com.cab404.acli;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.cab404.ponyscape.utils.Anim;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cab404
 */
public class ACLIList implements FragmentedList {
	protected List<Part> parts;
	protected ViewGroup root;
	protected Handler threading;

	protected ACLIList() {}

	public ACLIList(ViewGroup root) {
		this.root = root;
		parts = new ArrayList<>();
		threading = new Handler(root.getContext().getMainLooper());
	}

	public void add(Part part) {
		add(part, root.getChildCount());
	}

	public void add(Part part, int index) {
		part.onInsert(this);

		parts.add(index, part);

		View view = part.create(LayoutInflater.from(root.getContext()), root, root.getContext());

		if (view == null) throw new RuntimeException("Part returning null view!");

		LinearLayout layout = new LinearLayout(root.getContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(view);

		root.addView(layout, index);

	}

	public void remove(Part part) {
		if (parts.contains(part)) {

			int index = parts.indexOf(part);

			part.onRemove(root.getChildAt(parts.indexOf(part)), root, root.getContext());

			root.removeViewAt(index);
			parts.remove(index);

		}
	}

	public void removeSlowly(final Part part) {
		if (parts.contains(part)) {
			int index = parts.indexOf(part);
			final LinearLayout view = (LinearLayout) root.getChildAt(index);

			Anim.fadeOut(root.getChildAt(index), 200, new Runnable() {
				@Override public void run() {
					view.getLayoutParams().width = view.getChildAt(0).getWidth();
					view.getLayoutParams().height = view.getChildAt(0).getHeight();

					view.removeViewAt(0);

					view.requestLayout();

					Anim.resize(view, 0, -1, 200, new Runnable() {
						@Override public void run() {
							remove(part);
						}
					});
				}
			});
		}
	}

	public void hide(final Part part) {
		if (parts.contains(part)) {
			int index = parts.indexOf(part);
			final LinearLayout view = (LinearLayout) root.getChildAt(index);

			Anim.fadeOut(root.getChildAt(index), 200, new Runnable() {
				@Override public void run() {
					view.getLayoutParams().width = view.getChildAt(0).getWidth();
					view.getLayoutParams().height = view.getChildAt(0).getHeight();

					view.getChildAt(0)
							.setVisibility(View.GONE);

					view.requestLayout();

					Anim.resize(view, 0, -1, 200, null);
				}
			});
		}
	}

	public void show(final Part part) {
		if (parts.contains(part)) {
			int index = parts.indexOf(part);
			final LinearLayout view = (LinearLayout) root.getChildAt(index);
			view.getChildAt(0)
					.setVisibility(View.VISIBLE);
			Anim.resize(view, view.getChildAt(0).getHeight(), -1, 200, new Runnable() {
				@Override public void run() {
					view.getChildAt(0)
							.setVisibility(View.VISIBLE);
					view.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
					view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
					Anim.fadeIn(view, 200);
				}
			});
			view.getChildAt(0)
					.setVisibility(View.GONE);
		}
	}

	public int size() {
		return parts.size();
	}

	@Override public Context getContext() {
		return root.getContext();
	}

	public Part partAt(int index) {
		return parts.get(index);
	}

	@Override public int indexOf(Part part) {
		return parts.indexOf(part);
	}

}
