package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Topic;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.events.Commands;
import com.cab404.ponyscape.utils.Bus;
import com.cab404.ponyscape.utils.DateUtils;
import com.cab404.ponyscape.utils.TextEscaper;

/**
 * @author cab404
 */
public class TopicPart extends Part {

	private Topic topic;

	public TopicPart(Topic topic) {
		this.topic = topic;
	}

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, final Context context) {
		View view = inflater.inflate(R.layout.part_topic, viewGroup, false);

		((TextView) view.findViewById(R.id.title))
				.setText(topic.title);
		view.findViewById(R.id.title)
				.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View view) {
						Bus.send(new Commands.Run("post load " + topic.id));
					}
				});

		((TextView) view.findViewById(R.id.text))
				.setText(TextEscaper.simpleEscape(topic.text, context));
		((TextView) view.findViewById(R.id.data))
				.setText(topic.author.login + ", " + DateUtils.convertToString(topic.date, context));
		((TextView) view.findViewById(R.id.rating))
				.setText(topic.votes);
		((TextView) view.findViewById(R.id.tags))
				.setText(SU.join(topic.tags, ", "));

		((TextView) view.findViewById(R.id.id)).setText("#" + topic.id);

		return view;
	}

	@Override protected void update(View view, ViewGroup parent, Context context) {
		super.update(view, parent, context);
		((TextView) view.findViewById(R.id.data))
				.setText(topic.author.login + ", " + DateUtils.convertToString(topic.date, context));
	}

	@Override public void delete() {
		super.delete();
	}
}
