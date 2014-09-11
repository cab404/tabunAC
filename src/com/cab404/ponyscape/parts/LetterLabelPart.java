package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.LetterLabel;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.events.Commands;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.ViewSugar;

/**
 * Краткий заголовок письма.
 *
 * @author cab404
 */
public class LetterLabelPart extends Part {
	private LetterLabel data;

	public LetterLabelPart(LetterLabel label) {
		this.data = label;
	}

	@ViewSugar.Bind(R.id.title)
	private TextView label;

	@ViewSugar.Bind(R.id.recipients)
	private TextView recipients;

	@ViewSugar.Bind(R.id.date)
	private TextView date;

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		View view = inflater.inflate(R.layout.part_letter_label, viewGroup, false);

		view.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				Static.bus.send(new Commands.Run("mail load " + data.id));
			}
		});

		ViewSugar.bind(this, view);

		if (data.is_new)
			view.setBackgroundColor(
					context.getResources().getColor(R.color.bg_item_new)
			);
		else
			view.setBackgroundColor(
					context.getResources().getColor(R.color.bg_item)
			);

		label.setText(SU.deEntity(data.title));
		recipients.setText(Simple.buildRecipients(context, data));
//		date.setText(DateUtils.convertToString(data.date, context));

		return view;
	}
}
