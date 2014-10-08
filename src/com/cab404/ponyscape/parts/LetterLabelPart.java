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
import com.cab404.ponyscape.bus.AppContextExecutor;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.views.ViewSugar;
import com.cab404.sjbus.Bus;

/**
 * Краткий заголовок письма.
 *
 * @author cab404
 */
public class LetterLabelPart extends Part {
	private LetterLabel data;
	private Context context;

	public LetterLabelPart(LetterLabel label) {
		this.data = label;
	}

	@ViewSugar.Bind(R.id.title)
	private TextView label;

	@ViewSugar.Bind(R.id.recipients)
	private TextView recipients;

	@ViewSugar.Bind(R.id.date)
	private TextView date;

	private boolean selected;

	private void select() {
		selected = !selected;
		view.setBackgroundColor(
				getContext()
						.getResources()
						.getColor(
								selected ? R.color.bg_item_selected : R.color.bg_item
						)
		);
	}

	private void updateNew() {
		if (data.is_new)
			view.setBackgroundColor(
					context.getResources().getColor(R.color.bg_item_new)
			);
		else
			view.setBackgroundColor(
					context.getResources().getColor(R.color.bg_item)
			);
	}

	@Bus.Handler
	public void handleSelection(E.Letters.SelectAll e) {
		select();
	}

	@Bus.Handler
	public void hi(E.Letters.CallSelected e) {
		if (selected)
			e.ids.add(data.id);
	}


	@Bus.Handler(executor = AppContextExecutor.class)
	public void handleDeletion(E.Letters.UpdateDeleted e) {
		if (e.ids.contains(data.id))
			delete();
		else {
			selected = true;
			select();
		}
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void handleRead(E.Letters.UpdateNew e) {
		if (e.ids.contains(data.id)) {
			data.is_new = false;
			updateNew();
		} else {
			selected = true;
			select();
		}

	}

	View view;
	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		Static.bus.register(this);
		this.context = context;
		view = inflater.inflate(R.layout.part_letter_label, viewGroup, false);

		view.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				E.Letters.CallSelected sel = new E.Letters.CallSelected();
				Static.bus.send(sel);
				if (sel.ids.isEmpty())
					Static.bus.send(new E.Commands.Run("mail load " + data.id));
				else
					select();
			}
		});
		view.setOnLongClickListener(new View.OnLongClickListener() {
			@Override public boolean onLongClick(View v) {
				select();
				return true;
			}
		});

		ViewSugar.bind(this, view);
		updateNew();

		label.setText(SU.deEntity(data.title));
		recipients.setText(Simple.buildRecipients(context, data));
//		date.setText(DateUtils.convertToString(data.date, context));

		return view;
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		super.onRemove(view, parent, context);
		Static.bus.unregister(this);
	}
}
