package com.cab404.acli;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

		root.addView(view, index);

		threading.postDelayed(new ViewUpdater(part), part.updateDelay());

	}

	public void remove(Part part) {
		if (parts.contains(part)) {

			int index = parts.indexOf(part);

			part.onRemove(root.getChildAt(parts.indexOf(part)), root, root.getContext());

			root.removeViewAt(index);
			parts.remove(index);

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


	protected class ViewUpdater implements Runnable {

		private Part part;

		protected ViewUpdater(Part part) {
			this.part = part;
		}

		@Override public void run() {
			if (parts.contains(part)) {
				part.update(root.getChildAt(parts.indexOf(part)), root, root.getContext());
				threading.postDelayed(this, part.updateDelay());
			}
		}

	}

}
