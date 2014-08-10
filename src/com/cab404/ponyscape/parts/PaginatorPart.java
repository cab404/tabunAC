package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Paginator;
import com.cab404.ponyscape.R;

/**
 * @author cab404
 */
public class PaginatorPart extends Part {
	final Paginator paginator;

	public PaginatorPart(Paginator paginator) {
		this.paginator = paginator;
	}

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		View view = inflater.inflate(R.layout.part_paginator, viewGroup, false);

		return view;
	}
}
