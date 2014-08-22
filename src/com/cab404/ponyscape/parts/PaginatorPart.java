package com.cab404.ponyscape.parts;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Paginator;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.events.Commands;
import com.cab404.ponyscape.utils.Static;

/**
 * Отвечает за переходы между страницами.
 *
 * @author cab404
 */
public class PaginatorPart extends Part {
	private final Paginator paginator;

	public PaginatorPart(Paginator paginator) {
		this.paginator = paginator;
	}

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		View view = inflater.inflate(R.layout.part_paginator, viewGroup, false);
		((TextView) view.findViewById(R.id.current)).setText("Стр. " + paginator.page);
		((TextView) view.findViewById(R.id.count)).setText("Всего стр. " + paginator.maximum_page);

		if (paginator.page == 1)
			view.findViewById(R.id.back).setVisibility(View.INVISIBLE);
		else
			view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					Static.bus.send(new Commands.Run("page load \"" + Uri.parse(paginator.prev_href).getPath() + "\""));
				}
			});

		if (paginator.page == paginator.maximum_page)
			view.findViewById(R.id.next).setVisibility(View.INVISIBLE);
		else
			view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					Static.bus.send(new Commands.Run("page load \"" + Uri.parse(paginator.next_hrev).getPath() + "\""));
				}
			});


		((TextView) view.findViewById(R.id.back)).setText("Стр. " + (paginator.page - 1));
		((TextView) view.findViewById(R.id.next)).setText("Стр. " + (paginator.page + 1));

		return view;
	}
}
