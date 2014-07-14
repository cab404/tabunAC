package com.cab404.acli;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Simple fragment-like thing.
 *
 * @author cab404
 */
public abstract class Part {

	private FragmentedList parent = null;

	protected Context getContext() {
		return parent.getContext();
	}

	/**
	 * Called upon insertion.
	 */
	protected abstract View create(LayoutInflater inflater, ViewGroup viewGroup, Context context);

	/**
	 * Called every so often, {@link Part#updateDelay()}  }
	 */
	protected void update(View view, ViewGroup parent, Context context) {}

	/**
	 * Specifying update period
	 */
	protected long updateDelay() { return 10000;}

	/**
	 * Called upon removal of fragment.
	 */
	protected void onRemove(View view, ViewGroup parent, Context context) {}

	/**
	 * Inserts a new view after this one
	 */
	protected void insertAfter(Part part) {
		parent.add(part, parent.indexOf(this));
	}

	protected void onInsert(FragmentedList parent) {
		this.parent = parent;
	}

	/**
	 * Removes self from list
	 */
	protected void delete() {
		if (parent == null)
			throw new RuntimeException("super.onInsert() was not called.");
		parent.remove(this);
	}

}
