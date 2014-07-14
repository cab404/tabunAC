package com.cab404.acli;

import android.content.Context;

/**
 * @author cab404
 */
public interface FragmentedList {

	public void add(Part part);
	public void add(Part part, int index);

	public void remove(Part part);

	public Part partAt(int index);
	public int indexOf(Part part);

	public int size();

	public Context getContext();

}
