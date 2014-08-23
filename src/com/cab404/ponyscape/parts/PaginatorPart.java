package com.cab404.ponyscape.parts;

import android.content.Context;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Paginator;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.events.Commands;
import com.cab404.ponyscape.utils.Static;

import java.util.List;

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
		((TextView) view.findViewById(R.id.current)).setText("Сейчас мы на " + paginator.page + " странице");
		((TextView) view.findViewById(R.id.count)).setText(
				"Всего тут "
						+ paginator.maximum_page
						+ " "
						+ context.getResources().getQuantityString(R.plurals.pages, paginator.maximum_page)
		);

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


		((EditText) view.findViewById(R.id.go_to)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				int newPage = Integer.parseInt(v.getText().toString());
				if (newPage > 0 && newPage <= paginator.maximum_page) {
					List<String> segments = Uri.parse(paginator.next_hrev).getPathSegments();
					Static.bus.send(new Commands.Run(
							"page load \"/"
									+
									SU.join(segments.subList(0, segments.size() - 1), "/") + "/page" + newPage + "\""));
					return true;
				} else return false;
			}
		});

		((TextView) view.findViewById(R.id.back)).setText("Стр. " + (paginator.page - 1));
		((TextView) view.findViewById(R.id.next)).setText("Стр. " + (paginator.page + 1));

		return view;
	}
}
