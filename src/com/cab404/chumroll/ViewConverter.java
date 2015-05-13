package com.cab404.chumroll;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author cab404
 */
public interface ViewConverter<From> {
    public void convert(View view, From data, ViewGroup parent);
    public View createView(ViewGroup parent, LayoutInflater inflater);
    public boolean enabled(From data);
}
