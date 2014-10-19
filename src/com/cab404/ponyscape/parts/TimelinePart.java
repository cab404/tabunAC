package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.TimelineEntry;
import com.cab404.libtabun.requests.TimelineRequest;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;

import java.util.List;

/**
 * @author cab404
 */
public class TimelinePart extends Part {
	private View view;
	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, final Context context) {
		view = inflater.inflate(R.layout.part_timeline, viewGroup, false);
		view.findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				update();
			}
		});
		update();
		return view;
	}

	private void update() {

		final ListView list = (ListView) view.findViewById(R.id.timeline);
		Static.bus.send(new E.Commands.Run("luna"));
		Static.bus.send(new E.Status("Обновляю ленту..."));
		Thread thread = new Thread() {
			@Override public void run() {
				final TimelineRequest request = new TimelineRequest();
				request.exec(Static.user);

				if (request.success()) {
					Static.handler.post(
							new Runnable() {
								@Override public void run() {
									list.setAdapter(new TimelineAdapter(request.timeline));
								}
							}
					);
				} else {
					Static.bus.send(new E.Commands.Failure("Не вышло обновить ленту."));
				}

				Static.bus.send(new E.Commands.Finished());
			}
		};
		thread.start();

	}


	private static class TimelineAdapter extends BaseAdapter {

		private final List<TimelineEntry> timeline;
		public TimelineAdapter(List<TimelineEntry> timeline) {
			this.timeline = timeline;
		}

		@Override public int getCount() {
			return timeline.size();
		}

		@Override public Object getItem(int position) {
			return timeline.get(position);
		}

		@Override public long getItemId(int position) {
			return 0;
		}

		@Override public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			if (convertView == null)
				convertView = inflater.inflate(R.layout.part_timeline_entry, parent, false);

			final TimelineEntry e = timeline.get(position);

			((TextView) convertView.findViewById(R.id.first_line)).setText(
					SU.deEntity(
							e.commenter.login + " в блоге " + e.topic.blog.name
					)
			);

			((TextView) convertView.findViewById(R.id.topic_name)).setText(
					SU.deEntity(
							e.topic.title
					)
			);

			((TextView) convertView.findViewById(R.id.comment_count)).setText(
					e.topic.comments + " " + parent
							.getContext()
							.getResources()
							.getQuantityString(R.plurals.comments, e.topic.comments)
			);

			convertView.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					Static.bus.send(new E.Commands.Run("post by_comment " + e.comment_id));
				}
			});

			return convertView;
		}

	}

}
