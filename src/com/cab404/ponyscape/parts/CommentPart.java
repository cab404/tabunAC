package com.cab404.ponyscape.parts;

import android.animation.Animator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Comment;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.AppContextExecutor;
import com.cab404.ponyscape.bus.events.Commands;
import com.cab404.ponyscape.bus.events.DataAcquired;
import com.cab404.ponyscape.utils.Anim;
import com.cab404.ponyscape.utils.DateUtils;
import com.cab404.ponyscape.utils.HtmlRipper;
import com.cab404.ponyscape.utils.Static;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */
public class CommentPart extends Part {

	private CharSequence text = null;
	public final Comment comment;
	private HtmlRipper ripper;

	View view;
	public CommentPart(Comment comment) {
		Static.bus.register(this);
		this.comment = comment;
	}

	@Override public View create(LayoutInflater inflater, ViewGroup viewGroup, final Context context) {
		view = inflater.inflate(R.layout.part_comment, viewGroup, false);
		convert(view, context);
		return view;
	}

	public void convert(final View view, Context context) {
		this.view = view;
		view.findViewById(R.id.footer).setVisibility(View.GONE);

		if (ripper == null) {
			ripper = new HtmlRipper((ViewGroup) view.findViewById(R.id.content));
			ripper.escape(comment.text);
		} else {
			ripper.changeLayout((ViewGroup) view.findViewById(R.id.content));
		}

		((TextView) view.findViewById(R.id.data))
				.setText(comment.author.login + ", " + DateUtils.convertToString(comment.date, context));
		((TextView) view.findViewById(R.id.rating))
				.setText(comment.votes > 0 ? "+" + comment.votes : "" + comment.votes);

		view.findViewById(R.id.plus).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				Static.bus.send(new Commands.Run("votefor comment " + comment.id + " 1"));
			}
		});

		view.findViewById(R.id.minus).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				Static.bus.send(new Commands.Run("votefor comment " + comment.id + " -1"));
			}
		});

		view.setOnLongClickListener(new View.OnLongClickListener() {
			@Override public boolean onLongClick(View v) {
				View foo = view.findViewById(R.id.footer);
				if (foo.getVisibility() == View.GONE)
					foo.setVisibility(View.VISIBLE);
				else
					foo.setVisibility(View.GONE);
				return true;
			}
		});

		((TextView) view.findViewById(R.id.id)).setText("#" + comment.id);
	}

	public void kill() {
		Static.bus.unregister(this);
		ripper.destroy();
		ripper = null;
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void handleVoteChange(DataAcquired.CommentVote vote) {
		if (comment.id == vote.id) {
			comment.votes = vote.votes;
			final TextView rating = (TextView) view.findViewById(R.id.rating);

			rating.animate().scaleX(0).setDuration(100).setListener(new Anim.AnimatorListenerImpl() {
				@Override public void onAnimationEnd(Animator animation) {
					rating.setText((comment.votes > 0 ? "+" : "") + comment.votes);
					rating.animate().scaleX(1).setDuration(100).setListener(null);
				}
			});
		}
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		super.onRemove(view, parent, context);
		kill();
	}
}