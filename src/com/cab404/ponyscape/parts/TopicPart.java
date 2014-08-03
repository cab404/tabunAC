package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Topic;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.utils.Anim;
import com.cab404.ponyscape.utils.DateUtils;
import com.cab404.ponyscape.utils.TextEscaper;

/**
 * @author cab404
 */
public class TopicPart extends Part {

	private Topic topic;
	private ViewGroup view;

	public TopicPart(Topic topic) {
		this.topic = topic;
	}

	@Override protected View create(LayoutInflater inflater, final ViewGroup viewGroup, final Context context) {
		view = (ViewGroup) inflater.inflate(R.layout.part_topic, viewGroup, false);

		((TextView) view.findViewById(R.id.title))
				.setText(topic.title);
		view.findViewById(R.id.title)
				.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View viewsssss) {
//						Static.bus.send(new Commands.Run("post load " + topic.id));
						Anim.capsulize(view);
					}
				});


		TextEscaper.escape(topic.text,
				(LinearLayout) view.findViewById(R.id.content)
		);

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


	int initial_height = 0;
	public void hide() {
		initial_height = view.getHeight();
		Anim.resize(view, 0, -1, 200, null);
	}

	public void show() {
		Anim.resize(view, initial_height, -1, 200, null);
	}

	@Override public void delete() {
		super.delete();
	}
}
