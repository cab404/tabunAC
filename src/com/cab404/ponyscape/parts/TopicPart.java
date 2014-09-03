package com.cab404.ponyscape.parts;

import android.animation.Animator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Topic;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.AppContextExecutor;
import com.cab404.ponyscape.bus.events.Commands;
import com.cab404.ponyscape.bus.events.DataAcquired;
import com.cab404.ponyscape.bus.events.Parts;
import com.cab404.ponyscape.utils.DateUtils;
import com.cab404.ponyscape.utils.HtmlRipper;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.views.animation.Anim;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */
public class TopicPart extends Part {

	public final Topic topic;
	private ViewGroup view;

	public TopicPart(Topic topic) {
		this.topic = topic;
	}
	private HtmlRipper ripper;

	@Override protected View create(LayoutInflater inflater, final ViewGroup viewGroup, final Context context) {
		Static.bus.register(this);
		view = (ViewGroup) inflater.inflate(R.layout.part_topic, viewGroup, false);

		((TextView) view.findViewById(R.id.text))
				.setText(SU.deEntity(topic.title));
		view.findViewById(R.id.text)
				.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View unused) {
						Static.bus.send(new Commands.Run("post load " + topic.id));
					}
				});


		ripper = new HtmlRipper((ViewGroup) view.findViewById(R.id.content));
		ripper.escape(topic.text);
		view.findViewById(R.id.content).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			int last_width = 0;
			@Override public void onLayoutChange(View v, int l, int t, int r, int b, int oL, int oT, int oR, int oB) {
				/*
				 * Запускаем обновление раскладки только при изменении ширины.
				 * Если убрать, то WebView заспамит в лог кучу ошибок: так он кидает только одну :D
				 */
				if (last_width != view.getWidth()) {
					last_width = view.getWidth();
					ripper.layout();
				}
			}
		});

		((TextView) view.findViewById(R.id.data))
				.setText(topic.author.login
						+ " в блоге '" + SU.deEntity(topic.blog.name)
						+ "'" + ", " + DateUtils.convertToString(topic.date, context));

		// Для бегущей строки.
		view.findViewById(R.id.data)
				.setSelected(true);

		((TextView) view.findViewById(R.id.rating))
				.setText(topic.votes);

		view.findViewById(R.id.plus).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				Static.bus.send(new Commands.Run("votefor post " + topic.id + " 1"));
			}
		});

		view.findViewById(R.id.zero).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				Static.bus.send(new Commands.Run("votefor post " + topic.id + " 0"));
			}
		});

		view.findViewById(R.id.minus).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				Static.bus.send(new Commands.Run("votefor post " + topic.id + " -1"));
			}
		});

		((TextView) view.findViewById(R.id.tags))
				.setText(SU.deEntity(SU.join(topic.tags, ", ")));

		StringBuilder info = new StringBuilder("#" + topic.id);

		if (topic.comments != 0) {
			info
					.append('\n')
					.append(topic.comments)
					.append(" ")
					.append(context.getResources().getQuantityString(R.plurals.Mail_Label_Comments, topic.comments));
			if (topic.comments_new != 0)
				info
						.append(", ")
						.append(topic.comments_new)
						.append(" ")
						.append(context.getResources().getQuantityString(R.plurals.Mail_Label_NewComments, topic.comments_new));
		}

		((TextView) view.findViewById(R.id.id)).setText(info);


//		view.setAlpha(0);
//		view.animate().alpha(1).setDuration(200);

		return view;
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void handleVoteChange(DataAcquired.PostVote vote) {
		if (vote.id == topic.id) {
			topic.votes = (vote.votes > 0 ? "+" : "") + vote.votes;
			final TextView rating = ((TextView) view.findViewById(R.id.rating));

			// Скрываем и показываем уже изменённый рейтинг.
			rating.animate().scaleX(0).setDuration(100).setListener(new Anim.AnimatorListenerImpl() {
				@Override public void onAnimationEnd(Animator animation) {
					((TextView) view.findViewById(R.id.rating)).setText(topic.votes);
					rating.animate().scaleX(1).setDuration(100).setListener(null);
				}
			});
		}
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		super.onRemove(view, parent, context);
		Static.bus.unregister(this);

		ripper.destroy();
	}

	//	int initial_height = 0;
	public void hide() {
//		initial_height = view.getHeight();
//		Anim.resize(view, 0, -1, 200, null);
		Static.bus.send(new Parts.Hide(this));
	}

	public void show() {
		Static.bus.send(new Parts.Show(this));
//		Anim.resize(view, initial_height, -1, 200, new Runnable() {
//			@Override public void run() {
//				view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
//			}
//		});
	}

}
